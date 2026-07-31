package org.datayoo.oyez.op.processing.generator;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.generator.RandTimeDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.Random;

@OpDefiner(name = "RandCellPhone",
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
public class RandCellPhone extends BaseProcessOperator {
  protected int columnIndex = -1;
  protected String columnName;

  protected String[] segments = new String[] {
      //移动
      "134", "135", "136", "137", "138", "139", "147", "150", "151", "152",
      "157", "158", "159", "165", "172", "178", "182", "183", "184", "187",
      "188", "195", "197", "198",
      //联通
      "130", "131", "132", "155", "156", "185", "186", "145", "175", "176",
      //电信
      "133", "153", "177", "173", "180", "181", "189", "199"
  };

  public RandCellPhone(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    columnName = groupParameter.getParameterValue(
        RandTimeDescriptor.PARAM_COLUMN_NAME);
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
    if (columnIndex == -1) {
      columnIndex = columnSetMetadata.getColumnIndex(columnName);
    }
    objects[columnIndex] = getRandomCellPhone();
    return objects;
  }

  private String getRandomCellPhone() {
    Random r = new Random();
    StringBuilder sb = new StringBuilder();
    int index = r.nextInt(segments.length);
    sb.append(segments[index]);
    for (int i = 0; i < 8; i++) {
      sb.append(r.nextInt(9 + 1));
    }
    return sb.toString();
  }
}
