package org.datayoo.oyez.op.processing.r.filter;

import com.google.common.hash.Funnels;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.configx.parameter.RowParameter;
import org.datayoo.configx.parameter.TableParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.op.processing.AbstractResDataDependencyOperator;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.r.filter.BloomFilterDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@OpDefiner(name = "MultiColumnsBloomFilter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "oyez",
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_RESOURCE_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "",
    compoxes = {})
public class MultiColumnsBloomFilter extends AbstractResDataDependencyOperator {

  private String[] srcColumns;
  private String[] dataColumns;
  private int[] srcColumnIndexes;
  private int[] dataColumnIndexes;
  private double fpp;
  private String charset;

  private com.google.common.hash.BloomFilter bloomFilter;

  public MultiColumnsBloomFilter(FlowNodeMetadata operatorMetadata,
      FlowNode parent, EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void initializeResource(PlRowSet plRowSet) {
    ColumnSetMetadata columnSetMetadata = plRowSet.getColumnSetMetadata();
    for (int i = 0; i < srcColumns.length; i++) {
      srcColumnIndexes[i] = columnSetMetadata.getColumnIndex(srcColumns[i]);
    }
    bloomFilter = com.google.common.hash.BloomFilter.create(
        Funnels.byteArrayFunnel(), plRowSet.getRowsCount(),
        fpp);
    for (Object[] row : plRowSet.getRows()) {
      StringBuilder sb = new StringBuilder();
      for (int index : srcColumnIndexes) {
        sb.append(row[index]).append(";");
      }
      if (sb.toString().isEmpty()) {
        continue;
      }
      bloomFilter.put(sb.toString().getBytes(StandardCharsets.UTF_8));
    }
  }

  @Override
  protected void innerOperate(PlRowSet plRowSet) {
    int i = 0;
    ColumnSetMetadata columnSetMetadata = plRowSet.getColumnSetMetadata();
    PlRowSet outSet = this.createRowSet(columnSetMetadata);
    for (Object[] row : plRowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      try {
        Object[] out = this.innerOperate(columnSetMetadata, i++, row);
        if (out != null) {
          outSet.addRow(out);
        }
      } catch (Throwable var8) {
        if (!this.reportErrorAndRun(row, var8)) {
          throw new OperationInterruptionException(var8);
        }
      }
    }
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] row) {
    if (dataColumnIndexes == null) {
      int j = 0;
      dataColumnIndexes = new int[dataColumns.length];
      for (String dataColumn : dataColumns) {
        dataColumnIndexes[j] = columnSetMetadata.getColumnIndex(dataColumn);
        j++;
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int dataColumnIndex : dataColumnIndexes) {
      sb.append(row[dataColumnIndex]).append(";");
    }
    if (sb.toString().isEmpty()) {
      return null;
    }
    if (bloomFilter.mightContain(sb.toString().getBytes())) {
      return row;
    }
    return null;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    TableParameter columnSetParameter = (TableParameter) this.parameters.getParameter(
        "columnSet");
    srcColumns = new String[columnSetParameter.getParameters().size()];
    srcColumnIndexes = new int[columnSetParameter.getParameters().size()];
    dataColumns = new String[columnSetParameter.getParameters().size()];
    int i = 0;
    for (RowParameter rowParameter : columnSetParameter.getParameters()) {
      srcColumns[i] = rowParameter.getParameterValue(
          BloomFilterDescriptor.PARAM_SRC_COLUMN);
      dataColumns[i] = rowParameter.getParameterValue(
          BloomFilterDescriptor.PARAM_COLUMN);
      i++;
    }
    fpp = groupParameter.getParameterValueAsDouble(
        BloomFilterDescriptor.PARAM_FPP, 0.03);
    charset = groupParameter.getParameterValue(
        BloomFilterDescriptor.PARAM_CHARSET, "utf-8");
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
