package org.datayoo.sengee.op.processing.v.transformer;

import org.datayoo.datax.sd.*;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * @author tangtadin
 * @version 1.0
 * @description: TODO
 * @date 2021/7/13 4:12 PM
 */
@OpDefiner(name = "DictMapper",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,structure")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_DICT_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "    <parameter name=\"colName\" c_Alias=\"待映射列\" c_Compox=\"sengee-dataIn:all-column-selector\" c_Option=\"false\"></parameter>"
            + "    <parameter name=\"keyColumn\" c_Alias=\"字典key列\" c_Compox=\"sengee-dictIn:all-column-selector\" c_Option=\"false\"></parameter>"
            + "    <parameter name=\"valueColumn\" c_Alias=\"字典value列\" c_Compox=\"sengee-dictIn:all-column-selector\" c_Option=\"false\"></parameter>"
            + "    </parametergroup>" + "</parameters>",
    compoxes = {})
public class DictMapperDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_COL_NAME = "colName";
  public static final String PARAM_KEY_COL = "keyColumn";
  public static final String PARAM_VALUE_COL = "valueColumn";
  public static final String PARAM_SUFFIX = "_dictMapper";

  private String colName;

  private String valueColumn;

  public DictMapperDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    readParameters();
    ColumnSetMetadata columnSetMetadata = getPortColumnSetMetadata(
        SengeeOperatorConstants.PORT_DATA_IN);
    if (columnSetMetadata == null) {
      return new GeneralColumnSetMetadata(SengeeConstants.FDT_ANY);
    }
    GeneralColumnSetMetadata outColumnSetMetadata = new GeneralColumnSetMetadata(
        columnSetMetadata);
    columnSetMetadata = getPortColumnSetMetadata(
        SengeeOperatorConstants.PORT_DICT_IN);
    if (columnSetMetadata != null) {
      if (valueColumn != null && !valueColumn.isEmpty()) {
        if (columnSetMetadata.getColumn(valueColumn) != null) {
          ColumnMetadata columnMetadata = new GeneralColumnMetadata(
              String.format("%s%s", colName, PARAM_SUFFIX));
          columnMetadata.setType(
              columnSetMetadata.getColumn(valueColumn).getType());
          outColumnSetMetadata.addColumn(columnMetadata);
        }
      }
    }
    return outColumnSetMetadata;
  }

  @Override
  protected void readParameters() {
    colName = this.parameters.getParameterValue(PARAM_COL_NAME);
    valueColumn = this.parameters.getParameterValue(PARAM_VALUE_COL);
  }

  @Override
  protected void validateParameters() {
  }
}
