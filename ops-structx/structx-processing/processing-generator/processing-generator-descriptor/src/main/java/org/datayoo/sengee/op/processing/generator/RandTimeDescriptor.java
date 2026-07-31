package org.datayoo.sengee.op.processing.generator;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowContext;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@OpDefiner(name = "RandTime",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "generator")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"数据列\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"beginDate\" c_Option=\"false\" c_Alias=\"起始日期\" c_Compox=\"sightx-date\"></parameter>"
            + "<parameter name=\"beginTime\" c_Option=\"false\" c_Alias=\"起始时间\" c_Compox=\"sightx-time\"></parameter>"
            + "<parameter name=\"endDate\" c_Option=\"false\" c_Alias=\"结束日期\" c_Compox=\"sightx-date\"></parameter>"
            + "<parameter name=\"endTime\" c_Option=\"false\" c_Alias=\"结束时间\" c_Compox=\"sightx-time\"></parameter>"
            + "<parameter name=\"pattern\" c_Option=\"true\" c_Alias=\"日期格式\"></parameter>"
            + "</parametergroup></parameters>",
    compoxes = {})
public class RandTimeDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_BEGIN_DATE = "beginDate";
  public static final String PARAM_BEGIN_TIME = "beginTime";
  public static final String PARAM_END_DATE = "endDate";
  public static final String PARAM_END_TIME = "endTime";
  public static final String PARAM_COLUMN_NAME = "columnName";
  public static final String PARAM_PATTERN = "pattern";

  public RandTimeDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected void readParameters() {
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    FlowPort inputPort = this.inputPorts.iterator().next();
    return this.getPortColumnSetMetadata(inputPort.getName());
  }

  @Override
  protected void validateParameters() {
    //此处由于timestamp控件缺失，暂时这么处理
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String beginDateValue = this.parameters.getParameterValue(PARAM_BEGIN_DATE);
    String endDateValue = this.parameters.getParameterValue(PARAM_END_DATE);
    try {
      Date beginDate = dateFormat.parse(beginDateValue);
      Date endDate = dateFormat.parse(endDateValue);
      if (beginDate.after(endDate)) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_COL_GREATER, this.alias,
            PARAM_BEGIN_DATE, PARAM_END_DATE));
      }
    } catch (ParseException e) {
      throw new OperationRuntimeException(e);
    }
  }
}
