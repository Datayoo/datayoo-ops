package org.datayoo.oyez.op.processing.r.join;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.DataSetMap;
import org.datayoo.moql.DataSetMapImpl;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.util.MoqlUtils;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.LinkedList;
import java.util.List;

@OpDefiner(name = "Complement",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    replica = -1,
    inputPorts = { @Port(name = "leftIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = "rightIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class Complement extends BaseSetOperator {

  public Complement(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void innerOperate() {
    PlRowSet leftPlRowSet = readAll(leftInput);
    PlRowSet rightPlRowSet = readAll(rightInput);
    PlRowSet outSet = createRowSet(outputColumnSetMetadata);
    String watermark = getInputWaterMark();
    List<Object[]> leftRows = null;
    if (leftPlRowSet != null) {
      leftRows = leftPlRowSet.getRows();
    } else {
      leftRows = new LinkedList<>();
    }
    List<Object[]> rightRows = null;
    if (rightPlRowSet != null) {
      rightRows = rightPlRowSet.getRows();
    } else {
      rightRows = new LinkedList<>();
    }
    DataSetMap dataSetMap = new DataSetMapImpl();
    dataSetMap.putDataSet("ROW1", leftRows);
    dataSetMap.putDataSet("ROW2", rightRows);
    outSet.addRows(MoqlUtils.execute(sql, dataSetMap));
    outputPort.write(outSet, watermark);
  }

  @Override
  protected String buildSetSql(ColumnSetMetadata leftColumnSetMetadata,
      ColumnSetMetadata rightColumnSetMetadata) {
    List<ColumnMetadata> metadataList = leftColumnSetMetadata.getColumns();
    StringBuilder sb = new StringBuilder();
    sb.append("(select ");
    for (int i = 0; i < metadataList.size(); i++) {
      sb.append("leftRow[").append(i).append("] ")
          .append(metadataList.get(i).getName());
      if (i != metadataList.size() - 1) {
        sb.append(",");
      }
    }
    sb.append(" from ROW1 leftRow) ");
    sb.append("complementation ");
    sb.append("(select ");
    for (int i = 0; i < metadataList.size(); i++) {
      sb.append("rightRow[").append(i).append("] ")
          .append(metadataList.get(i).getName());
      if (i != metadataList.size() - 1) {
        sb.append(",");
      }
    }
    sb.append(" from ROW2 rightRow) ");
    return sb.toString();
  }

  @Override
  protected ColumnSetMetadata buildOutputColumnSetMetadata(
      ColumnSetMetadata leftColumnSetMetadata,
      ColumnSetMetadata rightColumnSetMetadata) {
    return leftColumnSetMetadata;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
  }

}
