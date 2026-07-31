package org.datayoo.sengee.op.processing.v.coding;

import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

@OpDefiner(name = "XXHash",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "column,coding")
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
            + "<parameter name=\"workingMode\" c_Option=\"false\" c_Alias=\"编码输出模式\" c_Compox=\"workingModeCtrl\"></parameter>"
            + "<parametertable name=\"columnSet\" c_Alias=\"待哈希列集合\" c_Compox=\"sengee-columns-selector\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"待哈希列\"></parameter>"
            + "</head></parametertable>" + "</parametergroup></parameters>",
    compoxes = {  @Compox(id = "workingModeCtrl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "overwrite",
            data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]"))

    })
public class XXHashDescriptor extends AbstractCodecDescriptor {

  public static final String COL_SUFFIX = "xx";

  public XXHashDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected String getColumnSuffix() {
    return COL_SUFFIX;
  }
}
