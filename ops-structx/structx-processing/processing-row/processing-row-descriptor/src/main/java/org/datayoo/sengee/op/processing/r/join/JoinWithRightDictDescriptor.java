package org.datayoo.sengee.op.processing.r.join;

import org.datayoo.base.types.DataType;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
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
@OpDefiner(name = "JoinWithRightDict",
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
            + "<parameter name=\"leftColumn\" c_Option=\"false\" c_Alias=\"左侧列\" c_Compox=\"sengee-leftIn:string-column-selector\"></parameter>"
            + "<parameter name=\"rightColumn\" c_Option=\"false\" c_Alias=\"右侧列\" c_Compox=\"sengee-rightIn:string-column-selector\"></parameter>"
            + "</parametergroup>" + "</parameters>",
    compoxes = {})
public class JoinWithRightDictDescriptor extends AbstractProcessingDescriptor
    implements CompliationCsmDependent {

  public static final String PARAM_LEFTCOLUMN = "leftColumn";

  public static final String PARAM_RIGHTCOLUMN = "rightColumn";

  private String leftColumn;

  private String rightColumn;

  public JoinWithRightDictDescriptor(FlowNodeMetadata flowNodeMetadata,
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
        String columnName = metadata.getName();
        if (rightColumn != null && !rightColumn.isEmpty()) {
          if (rightColumn.equals(columnName)) {
            continue;
          }
        }
        if (metadataList.contains(metadata)) {
          int index = columnMap.get(metadata.getName());
          metadata.setName(metadata.getName() + index);
          columnMap.put(metadata.getName(), index + 1);
          metadataList.add(metadata);
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
    leftColumn = parameters.getParameterValue(PARAM_LEFTCOLUMN);
    rightColumn = parameters.getParameterValue(PARAM_RIGHTCOLUMN);
  }

  @Override
  protected void validateParameters() {
    if (leftColumn.isEmpty()) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_PARAM_EMPTY, this.alias,
          PARAM_LEFTCOLUMN));
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

    DataType leftType = leftColumnTypeMap.get(leftColumn);
    DataType rightType = rightColumnTypeMap.get(rightColumn);
    if (!leftType.equals(rightType)) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_UNMATCHED_DATATYPE, this.alias,
          leftColumn, rightColumn));
    }
  }

}
