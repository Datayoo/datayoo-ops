package org.datayoo.oyez.op.processing.r.sampling;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.processing.r.sampling.SampleCtrl;
import org.datayoo.sengee.op.processing.r.sampling.StratifiedSamplingDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 分层抽样
 *
 * @author he
 * @date 2021-11-18
 */
@OpDefiner(name = "StratifiedSampling",
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
public class StratifiedSampling extends BaseProcessOperator {

  private SampleCtrl sample;

  private int sampleSize;

  private double sampleRatio;

  private String stratifiedColumn;

  private int lineNo;

  private Map<Object, Double> stratifiedResidue;

  private int stratifiedColumnIndex;

  public StratifiedSampling(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    this.lineNo = 0;
    this.sample = SampleCtrl.valueOf(parameters
        .getParameterValue(StratifiedSamplingDescriptor.PARAM_SAMPLE));
    this.sampleSize = parameters
        .getParameterValueAsInt(StratifiedSamplingDescriptor.PARAM_SAMPLE_SIZE,
            0);
    this.sampleRatio = parameters.getParameterValueAsDouble(
        StratifiedSamplingDescriptor.PARAM_SAMPLE_RATIO, 0.0d);
    this.stratifiedColumn = parameters.getParameterValue(
        StratifiedSamplingDescriptor.PARAM_STRATIFIED_COLUMN);
    this.stratifiedColumnIndex = -1;
    this.stratifiedResidue = new HashMap<>();
  }

  @Override
  protected void innerOperate() {

    PlRowSet rowSet = this.inputPort.read();

    if (sample == SampleCtrl.ABSOLUTE) {
      if (lineNo + rowSet.getRowsCount() < sampleSize) {
        outputPort.write(rowSet, inputPort.getWaterMark());
        lineNo += rowSet.getRowsCount();
        return;
      }
    }

    if (sample == SampleCtrl.RELATIVE) {
      if (stratifiedColumnIndex < 0) {
        stratifiedColumnIndex = rowSet.getColumnSetMetadata()
            .getColumnIndex(stratifiedColumn);
      }
    }
    List<Object[]> sampleData;
    if (sample == SampleCtrl.ABSOLUTE) {
      sampleData = absolute(rowSet.getRows());
    } else {
      sampleData = relative(rowSet.getRows());
    }

    if (sampleData.size() > 0) {
      PlRowSet sampleRowSet = createRowSet(rowSet.getColumnSetMetadata());
      sampleRowSet.addRows(sampleData);
      outputPort.write(sampleRowSet, inputPort.getWaterMark());
    }
  }

  private List<Object[]> absolute(List<Object[]> rows) {
    List<Object[]> result = new ArrayList<>();
    for (Object[] row : rows) {
      if (lineNo < sampleSize) {
        result.add(row);
        lineNo++;
      }
    }
    return result;
  }

  private List<Object[]> relative(List<Object[]> rows) {
    List<Object[]> result = new LinkedList<>();
    Map<Object, List<Object[]>> groupMap = rows.stream()
        .collect(Collectors.groupingBy(o -> o[stratifiedColumnIndex]));
    for (Map.Entry<Object, List<Object[]>> entry : groupMap.entrySet()) {
      double sampleInfo = entry.getValue().size() * sampleRatio;
      if (stratifiedResidue.get(entry.getKey()) != null) {
        sampleInfo += stratifiedResidue.get(entry.getKey());
        stratifiedResidue.remove(entry.getKey());
      }
      double residue = sampleInfo % 1;
      if (residue > 0d) {
        stratifiedResidue.put(entry.getKey(), residue);
      }
      int size = (new Double(sampleInfo)).intValue();
      if (size > 0) {
        result.addAll(entry.getValue().subList(0, size));
      }
    }
    return result;
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    throw new UnsupportedOperationException("");
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    throw new UnsupportedOperationException("");
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
