package org.datayoo.sengee.op.processing.st;

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
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@OpDefiner(name = "RenameColumns",
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
            + "<parametertable name=\"columnSet\" c_Alias=\"待重命名列集合\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待重命名列\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"newColumnName\" c_Option=\"false\" c_Alias=\"新列名\"></parameter>"
            + "</head></parametertable></parametergroup></parameters>",
    compoxes = {})
public class RenameColumnsDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_NEW_COLUMN_NAME = "newColumnName";

  private List<String> newColumns = new LinkedList<>();

  public RenameColumnsDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    GeneralColumnSetMetadata columnSetMetadata = new GeneralColumnSetMetadata(
        flowPort.getFlowDataType());
    int i = 0;
    ColumnSetMetadata inputColumnSetMetadata = getInputColumnSetMetadata();
    for (ColumnMetadata columnMetadata : inputColumnSetMetadata.getColumns()) {
      if (columns.contains(columnMetadata.getName())) {
        if (newColumns.get(i) == null) {
          i++;
          continue;
        }
        GeneralColumnMetadata generalColumnMetadata = new GeneralColumnMetadata(
            columnMetadata);
        generalColumnMetadata.setName(newColumns.get(i));
        columnSetMetadata.getColumns().add(generalColumnMetadata);
        i++;
      } else {
        columnSetMetadata.addColumn(columnMetadata);
      }
    }
    return columnSetMetadata;
  }

  @Override
  protected void readParameters() {
    TableParameter columnSetParameter = getColumnSetParameter();
    columns = new ArrayList<>(columnSetParameter.getParameters().size());
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      columns.add(rowParameter.getParameterValue(PARAM_COLUMN_NAME));
      newColumns.add(rowParameter.getParameterValue(PARAM_NEW_COLUMN_NAME));
    }
  }

}
