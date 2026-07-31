package org.datayoo.sengee.op.processing.flat;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.footstone.sightx.annotation.VisibleDesc;
import org.datayoo.footstone.sightx.vis.VisibleType;
import org.datayoo.moql.EntityMap;
import org.datayoo.moql.EntityMapImpl;
import org.datayoo.moql.MoqlException;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.flatter.StructureDataFlatter;
import org.datayoo.sengee.datax.flatter.json.JsonFlatter;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.reader.stream.AbstractSemiStructedReaderDescriptor;
import org.datayoo.sengee.op.util.MoqlExceptionHelper;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opd.OperatorDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * 把对象内属性展开
 */
@OpDefiner(name = "ObjectFlatter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,flat")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"objectColumn\" c_Option=\"false\" c_Alias=\"待转换列名称\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待保留列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名称\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "</head>" + "</parametertable>"
            + "<parametergroup name=\"structureMappings\" c_Alias=\"结构映射\" c_Compox=\"sengee-structureMappings\">"
            + "<parameter name=\"mappingName\" c_Alias=\"字段映射名\">o</parameter>"
            + "<parameter name=\"dataPath\" c_Alias=\"结构字段路径\">o</parameter>"
            + "<parameter name=\"filterMode\" c_Alias=\"过滤模式\" c_Compox=\"filterModeCtrl\">filter</parameter>"
            + "<parameter name=\"startPos\" c_Alias=\"起始位置\" c_Compox=\"startPosCtrl\">0</parameter>"
            + "<parameter name=\"endPos\" c_Alias=\"结束位置\" c_Compox=\"endPosCtrl\">-1</parameter>"
            + "<parameter name=\"filter\" c_Alias=\"过滤条件\" c_Compox=\"filterCtrl\"></parameter>"
            + "<parametertable name=\"structureFields\" c_Alias=\"结构字段映射\">"
            + "<head><parameter name=\"columnName\" c_Alias=\"输出列名\"></parameter>"
            + "<parameter name=\"dataPath\" c_Alias=\"结构字段路径\"></parameter>"
            + "<parameter name=\"columnType\" c_Alias=\"输出列数据类型\" c_Compox=\"sightx-datatype\"></parameter>"
            + "<parameter name=\"formatFunc\" c_Alias=\"数据转换函数\" c_Option=\"true\"></parameter>"
            + "</head>" + "</parametertable>" + "</parametergroup>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "filterModeCtrl",
        parentId = "sightx-switch",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "filter",
            data = "[{\"label\": \"过滤\", \"value\": \"filter\"}, {\"label\": \"索引\", \"value\": \"index\"}]")),
        @Compox(id = "startPosCtrl",
            parentId = "sightx-uinteger",
            visibleDescriptors = {
                @VisibleDesc(visibleType = VisibleType.VISIBLE,
                    condition = "filterMode == \"index\"")
            }), @Compox(id = "endPosCtrl",
        parentId = "sightx-integer",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "filterMode == \"index\"")
        }), @Compox(id = "filterCtrl",
        parentId = "sightx-input",
        visibleDescriptors = { @VisibleDesc(visibleType = VisibleType.VISIBLE,
            condition = "filterMode == \"filter\"")
        })
    })
public class ObjectFlatterDescriptor
    extends AbstractSemiStructedReaderDescriptor {

  public static final String PARAM_OBJECT_COLUMN = "objectColumn";

  public static final String PARAM_COLUMN_SET = "columnSet";

  public static final String PARAM_COLUMN_NAME = "columnName";

  protected String column;

  protected String[] retains;

  public ObjectFlatterDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    column = parameters.getParameterValue(PARAM_OBJECT_COLUMN);
    columnSet = loadColumnSetFromParameters();
    TableParameter columnSetParameter = (TableParameter) this.parameters.getParameter(
        PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    retains = new String[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      String column = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      retains[i] = column;
      i++;
    }
  }

  @Override
  protected ColumnSetMetadata createColumnSetMetadata(FlowPort flowPort) {
    FlowPort inputPort = this.inputPorts.iterator().next();
    ColumnSetMetadata columnSetMetadata1 = this.columnSetMetadataMap.get(
        inputPort.getName());
    ColumnSetMetadata output = new GeneralColumnSetMetadata(
        SengeeConstants.FDT_ANY);
    for (String columnName : retains) {
      if (StringUtils.isEmpty(columnName))
        continue;
      output.getColumns().add(columnSetMetadata1.getColumn(columnName));
    }
    if (columnSet != null && columnSet.size() > 0) {
      output.getColumns().addAll(columnSet);
    }
    return output;
  }

  @Override
  protected StructureDataFlatter createDataFlatter() {
    try {
      return new JsonFlatter(this.structureCollectionMetadata);
    } catch (MoqlException e) {
      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
          operand), e);
    }
  }

  protected EntityMap loadData(InputStream inputStream) {
    EntityMapImpl entityMap = new EntityMapImpl();
    entityMap.putEntity(OBJECT_ENTITY_NAME,
        new JsonParser().parse(new InputStreamReader(inputStream)));
    return entityMap;
  }

  @Override
  protected String getEntityType() {
    return SengeeOperatorConstants.ET_JSON;
  }

  @Override
  public String readAsText(String fromInputOp, String streamName, int size,
      String charset) {
    readParameters();
    OperatorDescriptor operatorDescriptor = (OperatorDescriptor) getParent(
        this.getParent());
    operatorDescriptor.compileColumnSetMetadata();
    FlowPort inputPort = this.inputPorts.iterator().next();
    ColumnSetMetadata columnSetMetadata = this.columnSetMetadataMap.get(
        inputPort.getName());
    ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column);
    if (columnMetadata != null) {
      return new GsonBuilder().setPrettyPrinting().create()
          .toJson(columnMetadata.getType());
    }
    return "NO DATA";
  }

  protected FlowNode getParent(FlowNode flowNode) {
    FlowNode parent = flowNode.getParent();
    if (parent == null) {
      return flowNode;
    } else {
      return getParent(parent);
    }
  }

  @Override
  protected void validateParameters() {
    for (String columnName : retains) {
      if (StringUtils.isEmpty(columnName)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
            PARAM_COLUMN_NAME));
      }
    }
    if (structureCollectionMetadata.getAllStructureFields().size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_STRUCTURE_FIELDS));
    }
  }

  @Override
  public List<String> getEntityTypes() {
    return null;
  }
}
