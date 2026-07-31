package org.datayoo.sengee.op.processing.r.grouping;

import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnMetadata;
import org.datayoo.datax.sd.PlRowSet;
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
import java.util.List;

/**
 * 获取不同值
 *
 * @author hhn
 */
@OpDefiner(name = "Distinct",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,grouping")
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
            + "<parametertable name=\"columnSet\" c_Alias=\"去重列\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"列名\"></parameter>"
            + "</head></parametertable>" + "</parametergroup></parameters>",
    compoxes = {})
public class DistinctDescriptor extends AbstractProcessingDescriptor {

  public DistinctDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata columnSetMetadata = getInputColumnSetMetadata();
    List<ColumnMetadata> columnMetadataList = new ArrayList<>();
    for (String column : columns) {
      ColumnMetadata columnMetadata = columnSetMetadata.getColumn(column);
      if (columnMetadata != null) {
        columnMetadataList.add(new GeneralColumnMetadata(columnMetadata));
      }
    }
    columnSetMetadata.setColumns(columnMetadataList);
    return columnSetMetadata;
  }
}
