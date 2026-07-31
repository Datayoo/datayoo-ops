package org.datayoo.oyez.op.processing.r.order;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.DataSetMap;
import org.datayoo.moql.DataSetMapImpl;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.MoqlUtils;
import org.datayoo.oyez.op.processing.util.ProcessOperatorHelper;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.colset.ColumnInfo;
import org.datayoo.sengee.op.processing.r.order.SortDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串截取
 *
 * @author hhn
 */
@OpDefiner(name = "Sort",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    replica = -1,
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class Sort extends BaseProcessOperator {

  private String[] columns;

  private String[] directions;

  private ColumnInfo[] columnInfos;

  private String sql;

  public Sort(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters
        .getParameter(SortDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new String[rowParameters.size()];
    directions = new String[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter
          .getParameterValue(SortDescriptor.PARAM_COLUMN_NAME);
      directions[i] = rowParameter
          .getParameterValue(SortDescriptor.PARAM_DIRECTION);
      i++;
    }
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = readAll(inputPort);
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    PlRowSet outSet = createRowSet(columnSetMetadata);
    List<Object[]> rows = rowSet.getRows();
    if (rows.size() > 0) {
      loadColumnIndex(columnSetMetadata);
      DataSetMap dataSetMap = new DataSetMapImpl();
      dataSetMap.putDataSet("ROWS", rows);
      outSet.addRows(pack(MoqlUtils.execute(sql, dataSetMap)));
      outputPort.write(outSet, inputPort.getWaterMark());
    }
  }

  //获取字段所在index
  private void loadColumnIndex(ColumnSetMetadata columnSetMetadata) {
    columnInfos = ProcessOperatorHelper
        .bindColumnInfos(columnSetMetadata, columns, directions);
    StringBuilder sb = new StringBuilder("select row from ROWS row order by ");
    for (int i = 0; i < columnInfos.length; i++) {
      if (i > 0) {
        sb.append(",");
      }
      sb.append("row[").append(columnInfos[i].getColumnIndex()).append("] ")
          .append(columnInfos[i].getPayload());
    }
    sql = sb.toString();
  }

  private List<Object[]> pack(List<Object[]> objects) {
    List<Object[]> result = new ArrayList<>(objects.size());
    for (Object[] object : objects) {
      result.add((Object[]) object[0]);
    }
    return result;
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return columnSetMetadata;
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

}
