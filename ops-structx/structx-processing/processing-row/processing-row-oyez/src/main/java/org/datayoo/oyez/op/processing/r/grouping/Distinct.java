package org.datayoo.oyez.op.processing.r.grouping;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.DataSetMap;
import org.datayoo.moql.DataSetMapImpl;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.MoqlUtils;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.r.grouping.DistinctDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.List;

/**
 * 获取不同值
 *
 * @author hhn
 */
@OpDefiner(name = "Distinct",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
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
public class Distinct extends BaseProcessOperator {

  private int[] indexes;

  private String[] columns;

  private String sql;

  public Distinct(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    ColumnSetMetadata outputSetMetaData = new GeneralColumnSetMetadata(
        SengeeOperatorConstants.PORT_DATA_OUT);
    for (int i = 0; i < indexes.length; i++) {
      outputSetMetaData.addColumn(new GeneralColumnMetadata(
          columnSetMetadata.getColumns().get(indexes[i])));
    }
    return outputSetMetaData;
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = readAll(inputPort);
    List<Object[]> rows = rowSet.getRows();
    if (rows.size() > 0) {
      ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
      loadColumnIndex(columnSetMetadata);
      PlRowSet outSet = createRowSet(
          createOutputColumnSetMetadata(columnSetMetadata));
      DataSetMap dataSetMap = new DataSetMapImpl();
      dataSetMap.putDataSet("ROWS", rows);
      outSet.addRows(MoqlUtils.execute(sql, dataSetMap));
      outputPort.write(outSet, inputPort.getWaterMark());
    }
  }

  private void loadColumnIndex(ColumnSetMetadata columnSetMetadata) {
    StringBuilder sb = new StringBuilder();
    sb.append("select distinct ");
    indexes = new int[columns.length];
    for (int i = 0; i < columns.length; i++) {
      indexes[i] = columnSetMetadata.getColumnIndex(columns[i]);
    }
    for (int i = 0; i < indexes.length; i++) {
      if (i != 0) {
        sb.append(",");
      }
      sb.append(" row[").append(indexes[i]).append("]");
    }
    sb.append(" from ROWS row");
    sql = sb.toString();

  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) parameters.getParameter(
        DistinctDescriptor.PARAM_COLUMN_SET);
    List<RowParameter> rowParameters = columnSetParameter.getParameters();
    columns = new String[rowParameters.size()];
    int i = 0;
    for (RowParameter rowParameter : rowParameters) {
      columns[i] = rowParameter.getParameterValue(
          DistinctDescriptor.PARAM_COLUMN_NAME);
      i++;
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
}
