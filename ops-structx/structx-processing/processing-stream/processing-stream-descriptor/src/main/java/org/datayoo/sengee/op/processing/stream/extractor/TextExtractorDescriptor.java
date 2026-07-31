package org.datayoo.sengee.op.processing.stream.extractor;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.AbstractStreamProcessorDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "TextExtractor",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS,
        value = "extractor,stream,txt")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_STREAM_IN,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_BLOCK_OUT,
        flowDataType = SengeeConstants.FDT_STREAM_BLOCK,
        option = true), @Port(name = SengeeOperatorConstants.PORT_STREAM_OUT,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"textLimit\" c_Option=\"false\" c_Alias=\"抽取出的文本大小限制\" c_Compox=\"sightx-uinteger\">100000</parameter>"
            + "<parameter name=\"ignorePageNo\" c_Option=\"false\" c_Alias=\"是否忽略页码\" c_Compox=\"sightx-switch\">false</parameter>"
            + "<parameter name=\"ignoreSuperscript\" c_Option=\"false\" c_Alias=\"是否忽略上下标\" c_Compox=\"sightx-switch\">false</parameter>"
            + "<parameter name=\"ignoreLink\" c_Option=\"false\" c_Alias=\"是否忽略Link\" c_Compox=\"sightx-switch\">false</parameter>"
            + "<parameter name=\"ignoreEmbededFileName\" c_Option=\"false\" c_Alias=\"是否忽略嵌入文件的名字\" c_Compox=\"sightx-switch\">false</parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class TextExtractorDescriptor extends AbstractStreamProcessorDescriptor {

  public TextExtractorDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata getPortDeclaredColumnSetMetadata(
      FlowPort<PlRowSet> flowPort) {
    if (flowPort.getFlowDataType()
        .startsWith(SengeeConstants.FDT_DATA_STREAM)) {
      return ColumnSetMetadataLibrary.createDataStreamMetadata();
    } else {
      return ColumnSetMetadataLibrary.createBlockMetadata(false);
    }
  }

  @Override
  protected void readParameters() {
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    if (flowPort.getName().equals(SengeeOperatorConstants.PORT_BLOCK_OUT)) {
      return ColumnSetMetadataLibrary.createBlockMetadata(false);
    } else {
      return ColumnSetMetadataLibrary.createDataStreamMetadata();
    }
  }

  @Override
  public void validateParameters() {
  }

}
