package org.datayoo.sengee.op.processing.r.join;

import org.datayoo.base.types.DataType;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opd.CompliationCsmDependent;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.sengee.util.OpParameterHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hhn
 */
@OpDefiner(name = "JoinWithRight",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    replica = -1,
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,join")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_LEFT_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_RIGHT_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"columnSet\" c_Alias=\"连接属性\"><head>"
            + "<parameter name=\"leftColumn\" c_Option=\"false\" c_Alias=\"左侧列\" c_Compox=\"sengee-leftIn:all-column-selector\"></parameter>"
            + "<parameter name=\"rightColumn\" c_Option=\"false\" c_Alias=\"右侧列\" c_Compox=\"sengee-rightIn:all-column-selector\"></parameter>"
            + "</head>" + "</parametertable>" + "</parametergroup>"
            + "</parameters>",
    compoxes = {})
public class JoinWithRightDescriptor extends AbstractProcessingDescriptor
    implements CompliationCsmDependent {

  public static final String PARAM_LEFTCOLUMN = "leftColumn";

  public static final String PARAM_RIGHTCOLUMN = "rightColumn";

  private List<String> leftColumns;

  private List<String> rightColumns;

  public JoinWithRightDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    Map<String, Integer> columnMap = new HashMap<>();
    ColumnSetMetadata leftMetadata = this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_LEFT_IN);
    ColumnSetMetadata rightMetadata = this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_RIGHT_IN);
    List<ColumnMetadata> metadataList = new ArrayList<>();
    if (leftMetadata != null && rightMetadata != null) {
      for (ColumnMetadata metadata : leftMetadata.getColumns()) {
        columnMap.put(metadata.getName(), 1);
        metadataList.add(metadata);
      }
      for (ColumnMetadata metadata : rightMetadata.getColumns()) {
        if (metadataList.contains(metadata)) {
          int index = columnMap.get(metadata.getName());
          ColumnMetadata columnMetadata = new GeneralColumnMetadata(
              metadata.getName() + index);
          columnMap.put(metadata.getName(), index + 1);
          columnMetadata.setType(metadata.getType());
          metadataList.add(columnMetadata);
        } else {
          metadataList.add(metadata);
        }
      }
    }
    ColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata("/");
    columnSetMetadata.setColumns(metadataList);
    return columnSetMetadata;
  }

  protected void readParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    leftColumns = new ArrayList<>(columnSetParameter.getParameters().size());
    rightColumns = new ArrayList<>(columnSetParameter.getParameters().size());
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      leftColumns.add(rowParameter.getParameterValue(PARAM_LEFTCOLUMN));
      rightColumns.add(rowParameter.getParameterValue(PARAM_RIGHTCOLUMN));
    }
  }

  @Override
  protected void validateParameters() {
    if (leftColumns.size() == 0) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          AbstractProcessingDescriptor.PARAM_COLUMN_SET));
    }
    ColumnSetMetadata leftMetadata = this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_LEFT_IN);
    ColumnSetMetadata rightMetadata = this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_RIGHT_IN);
    validateDataType(leftMetadata, rightMetadata);
  }

  private void validateDataType(ColumnSetMetadata left,
      ColumnSetMetadata right) {
    Map<String, DataType> leftColumnTypeMap = OpParameterHelper.toColumnTypeMap(
        left);
    Map<String, DataType> rightColumnTypeMap = OpParameterHelper.toColumnTypeMap(
        right);

    for (int i = 0; i < leftColumns.size(); i++) {
      DataType leftType = leftColumnTypeMap.get(leftColumns.get(i));
      DataType rightType = rightColumnTypeMap.get(rightColumns.get(i));
      if (!leftType.equals(rightType)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_UNMATCHED_DATATYPE, this.alias,
            leftColumns.get(i), rightColumns.get(i)));
      }
    }
  }

}
