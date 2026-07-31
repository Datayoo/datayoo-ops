package org.datayoo.sengee.op.processing.r.sampling;

import org.apache.commons.lang3.StringUtils;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.footstone.sightx.annotation.VisibleDesc;
import org.datayoo.footstone.sightx.vis.VisibleType;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * 分层抽样
 *
 * @author he
 */
@OpDefiner(name = "StratifiedSampling",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,sampling")
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
            + "<parameter name=\"sample\" c_Option=\"false\" c_Alias=\"抽样策略\" c_Compox=\"sampleCtrl\">ABSOLUTE</parameter>"
            + "<parameter name=\"sampleSize\" c_Option=\"false\" c_Alias=\"抽样数量\" c_Compox=\"sampling-sampleSize\">0</parameter>"
            + "<parameter name=\"sampleRatio\" c_Option=\"false\" c_Alias=\"抽样比例\" c_Compox=\"sampling-sampleRatio\">0.0</parameter>"
            + "<parameter name=\"stratifiedColumn\" c_Option=\"false\" c_Alias=\"分层列\" c_Compox=\"sampling-stratifiedColumn\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = { @Compox(id = "sampleCtrl",
        parentId = "sightx-select",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "ABSOLUTE",
            data = "[{\"label\": \"绝对值\", \"value\": \"ABSOLUTE\"}, {\"label\": \"相对比例\", \"value\": \"RELATIVE\"}]")),
        @Compox(id = "sampling-sampleSize",
            parentId = "sightx-uinteger",
            visibleDescriptors = {
                @VisibleDesc(visibleType = VisibleType.VISIBLE,
                    condition = "sample == \"ABSOLUTE\"")
            }),
        @Compox(id = "sampling-sampleRatio",
            parentId = "sightx-double",
            visibleDescriptors = {
                @VisibleDesc(visibleType = VisibleType.VISIBLE,
                    condition = "sample == \"RELATIVE\"")
            }),
        @Compox(id = "sampling-stratifiedColumn",
            parentId = "sengee-column-selector",
            visibleDescriptors = {
                @VisibleDesc(visibleType = VisibleType.VISIBLE,
                    condition = "sample == \"RELATIVE\"")
            })
    })
public class StratifiedSamplingDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_SAMPLE = "sample";

  public static final String PARAM_SAMPLE_SIZE = "sampleSize";

  public static final String PARAM_SAMPLE_RATIO = "sampleRatio";

  public static final String PARAM_STRATIFIED_COLUMN = "stratifiedColumn";

  protected SampleCtrl sample;

  protected int sampleSize;

  protected double sampleRatio;

  protected String stratifiedColumn;

  public StratifiedSamplingDescriptor(FlowNodeMetadata flowNodeMetadata,
      FlowNode parent, CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
    sample = SampleCtrl.valueOf(
        parameters.getParameterValue(PARAM_SAMPLE, SampleCtrl.ABSOLUTE.name()));
    sampleSize = parameters.getParameterValueAsInt(PARAM_SAMPLE_SIZE, 0);
    sampleRatio = parameters
        .getParameterValueAsDouble(PARAM_SAMPLE_RATIO, 0.0d);
    stratifiedColumn = parameters
        .getParameterValue(PARAM_STRATIFIED_COLUMN, "");
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  protected void validateParameters() {
    if (sample == SampleCtrl.ABSOLUTE) {
      if (sampleSize <= 0) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_OPD_COL_GREATER, this.alias,
                PARAM_SAMPLE_SIZE, "0"));
      }
    } else if (sample == SampleCtrl.RELATIVE) {
      if (StringUtils.isEmpty(stratifiedColumn)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_OPD_EMPTY_RESOURCE,
                this.alias, PARAM_STRATIFIED_COLUMN));
      }

      if (sampleRatio <= 0.0d || sampleRatio >= 1.0d) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource
            .format(OperatorsExceptionConstants.ECM_OPD_COL_BETWEEN, this.alias,
                PARAM_SAMPLE_RATIO, "0", "1"));
      }
      // TODO 校验ColumnMindaType 是否NOMINAL
    } else {
      throw new OperationRuntimeException(OperatorsI18nMessageResource
          .format(OperatorsExceptionConstants.ECM_PARAM_INVALID, this.alias,
              PARAM_SAMPLE));
    }
  }
}
