package org.datayoo.oyez.op.processing.generator;

import org.datayoo.base.types.DataTypeUtils;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.port.OyezInputPort;
import org.datayoo.oyez.port.OyezOutputPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.generator.RandTimeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@OpDefiner(name = "RandTime",
    type = OperatorProfileConstants.OC_INPUT,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters = "<parameters></parameters>",
    compoxes = {})
public class RandTime extends BaseProcessOperator {

  protected Date beginDate;
  protected Date endDate;
  protected String columnName;
  protected int columnIndex = -1;
  protected int interval;
  protected String pattern;

  protected boolean isString = false;

  protected SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
      "yyyy-MM-dd HH:mm:ss");

  public RandTime(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    String beginTime = String.format("%s %s",
        groupParameter.getParameter(RandTimeDescriptor.PARAM_BEGIN_DATE),
        groupParameter.getParameter(RandTimeDescriptor.PARAM_BEGIN_TIME));
    String endTime = String.format("%s %s",
        groupParameter.getParameter(RandTimeDescriptor.PARAM_END_DATE),
        groupParameter.getParameter(RandTimeDescriptor.PARAM_END_TIME));
    columnName = groupParameter.getParameterValue(
        RandTimeDescriptor.PARAM_COLUMN_NAME);
    pattern = groupParameter.getParameterValue(
        RandTimeDescriptor.PARAM_PATTERN);
    try {
      beginDate = simpleDateFormat.parse(beginTime);
      endDate = simpleDateFormat.parse(endTime);
      interval = (int) (endDate.getTime() / 1000 - beginDate.getTime() / 1000);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }

  @Override
  public ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    ColumnMetadata columnMetadata = columnSetMetadata.getColumn(columnName);
    if (columnIndex == -1) {
      columnIndex = columnSetMetadata.getColumnIndex(columnName);
      isString = columnMetadata.getType().equals(DataTypeUtils.STRING_TYPE);
      if (isString) {
        simpleDateFormat = new SimpleDateFormat(pattern);
      }
    }
    if (isString) {
      objects[columnIndex] = simpleDateFormat.format(getRandomDate());
    } else
      objects[columnIndex] = getRandomDate();
    return objects;
  }

  private Date getRandomDate() {
    Random r = new Random();
    int randomNumber = r.nextInt(interval + 1);
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(beginDate);
    calendar.add(Calendar.SECOND, randomNumber);
    return calendar.getTime();
  }
}
