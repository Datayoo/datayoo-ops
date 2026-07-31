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

@OpDefiner(name = "MurmurHash",
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
            + "</head></parametertable>"
            + "<parameter name=\"algorithm\" c_Option=\"false\" c_Alias=\"算法\" c_Compox=\"algorithmCtrl\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "algorithmCtrl",
        parentId = "sightx-combox",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(data =
            "[{\"label\": \"MurmurHash2_32\", \"value\": \"MurmurHash2_32\"},"
                + " {\"label\": \"MurmurHash2-64\", \"value\": \"MurmurHash2_64\"},"
                + " {\"label\": \"MurmurHash3-32\", \"value\": \"MurmurHash3_32\"},"
                + " {\"label\": \"MurmurHash3-32x86\", \"value\": \"MurmurHash3_32x86\"},"
                + " {\"label\": \"MurmurHash3-128\", \"value\": \"MurmurHash3_128\"},"
                + " {\"label\": \"MurmurHash3-128x64\", \"value\": \"MurmurHash3_128x64\"}]")),
        @Compox(id = "workingModeCtrl",
            parentId = "sightx-combox",
            visibleDescriptors = {},
            dataDescriptor = @DataDesc(defaultValue = "overwrite",
                data = "[{\"label\": \"列值覆盖\", \"value\": \"overwrite\"}, {\"label\": \"新增列\", \"value\": \"addColumn\"}]"))

    })
public class MurmurHashDescriptor extends AbstractCodecDescriptor {

  public static final String PARAM_MURMUR_ALGORITHM = "algorithm";

  public static final String COL_SUFFIX = "mur";

  public MurmurHashDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected String getColumnSuffix() {
    return COL_SUFFIX;
  }

  protected void validateParameters() {
    super.validateParameters();
    MurmurAlgorithm murmurAlgorithm = MurmurAlgorithm.valueOf(
        parameters.getParameterValue(PARAM_MURMUR_ALGORITHM,
            MurmurAlgorithm.MurmurHash2_32.name()));
  }

  public static enum MurmurAlgorithm {
    MurmurHash2_32,
    MurmurHash2_64,
    MurmurHash3_32,
    MurmurHash3_32x86,
    MurmurHash3_128,
    MurmurHash3_128x64
  }
}
