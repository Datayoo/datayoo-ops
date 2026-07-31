package org.datayoo.sengee.op.processing.r;

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
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * 特征统计
 */
@OpDefiner(name = "FeatureStatisticor",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset") },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_COLUMN_FEATURE,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"rowCount\" c_Option=\"false\" c_Alias=\"特征统计行限定\" c_Compox=\"sightx-uinteger\">0</parameter>"
            + "<parameter name=\"distinctRatio\" c_Option=\"false\" c_Alias=\"唯一值输出阈值\" c_Compox=\"sightx-pdouble\">0.4</parameter>"
            + "<parameter name=\"sampleCount\" c_Option=\"false\" c_Alias=\"唯一值采样数\" c_Compox=\"sightx-udouble\">5.0</parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class FeatureStatisticorDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_ROW_COUNT = "rowCount";
  public static final String PARAM_DISTINCT_RATIO = "distinctRatio";
  public static final String PARAM_SAMPLE_COUNT = "sampleCount";
  public static final double DEFAULT_DISTINCT_RATIO = 0.4;
  public static final double DEFAULT_SAMPLE_COUNT = 5;

  public FeatureStatisticorDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return ColumnSetMetadataLibrary.createColumnFeatureMetadata();
  }

  @Override
  protected void readParameters() {
  }

  @Override
  protected void innerInitialize() {

  }

  @Override
  protected void innerDestroy() {

  }

  @Override
  protected void validateParameters() {

  }
}
