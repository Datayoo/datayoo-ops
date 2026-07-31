package org.datayoo.sengee.op.processing.st;

import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeName;
import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.moql.MoqlException;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.mapper.ColumnMappingEntry;
import org.datayoo.sengee.datax.util.ExpressionFactory;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@OpDefiner(name = "AddColumns",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
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
            + "<parametertable name=\"columnSet\" c_Alias=\"待添加列集合\"><head horizontal=\"false\">"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\"></parameter>"
            + "<parameter name=\"columnType\" c_Option=\"false\" c_Alias=\"列类型\" c_Compox=\"sightx-datatype\">String</parameter>\n"
            + "<parameter name=\"columnExpr\" c_Option=\"false\" c_Alias=\"列值表达式\"></parameter>\n"
            + "</head>\n" + "</parametertable></parametergroup></parameters>",
    compoxes = {})

public class AddColumnsDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_COLUMN_TYPE = "columnType";
  public static final String PARAM_COLUMN_EXPR = "columnExpr";

  protected List<ColumnMappingEntry> mappingEntries;

  public AddColumnsDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  public static List<ColumnMappingEntry> loadMappingEntries(String alias,
      GroupParameter parameters) {
    List<ColumnMappingEntry> mappingEntries = new LinkedList<>();
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        PARAM_COLUMN_SET);
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      String colName = rowParameter.getParameterValue(PARAM_COLUMN_NAME);
      String colType = rowParameter.getParameterValue(PARAM_COLUMN_TYPE);
      // 校验数据类型是否合法
      DataType dataType = DataTypeUtils.createDataType(
          DataTypeName.valueOf(colType), null, null);
      ColumnMappingEntry mappingEntry = new ColumnMappingEntry(colName,
          dataType);
      String expression = rowParameter.getParameterValue(PARAM_COLUMN_EXPR);
      // 校验值表达式
      try {
        mappingEntry.setExpression(
            ExpressionFactory.createExpression(expression));
      } catch (MoqlException e) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, alias,
            expression), e);
      }
      mappingEntries.add(mappingEntry);
    }
    return mappingEntries;
  }

  @Override
  protected void readParameters() {
    super.readParameters();
    mappingEntries = loadMappingEntries(this.alias, parameters);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    GeneralColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        flowPort.getFlowDataType());
    // 获得输入列集
    ColumnSetMetadata inputColumnSetMetadata = getInputColumnSetMetadata();
    if (inputColumnSetMetadata != null)
      columnSetMetadata.getColumns()
          .addAll(inputColumnSetMetadata.getColumns());
    for (ColumnMappingEntry mappingEntry : mappingEntries) {
      columnSetMetadata.addColumn(mappingEntry.toColumnMetadata());
    }
    return columnSetMetadata;
  }

  @Override
  protected void validateParameters() {
    if (columns.size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_COLUMN_SET));
    } else {
      Set<String> columnSet = new HashSet();
      for (String column : columns) {
        if (!columnSet.add(column)) {
          throw new OperationRuntimeException(
              OperatorsI18nMessageResource.format(
                  OperatorsExceptionConstants.ECM_RESOURCE_DUPLICATED,
                  this.alias, column));
        }
      }
      //添加列，不仅添加的列不能重复，与输入列也不能重复
      if (this.columnSetMetadataMap != null) {
        ColumnSetMetadata inputColumnSetMetadata = getInputColumnSetMetadata();
        for (ColumnMetadata columnMetadata : inputColumnSetMetadata.getColumns()) {
          if (!columnSet.add(columnMetadata.getName())) {
            throw new OperationRuntimeException(
                OperatorsI18nMessageResource.format(
                    OperatorsExceptionConstants.ECM_RESOURCE_DUPLICATED,
                    this.alias, columnMetadata.getName()));
          }
        }
      }
    }

  }
}
