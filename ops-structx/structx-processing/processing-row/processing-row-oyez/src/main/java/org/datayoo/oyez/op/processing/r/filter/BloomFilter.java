package org.datayoo.oyez.op.processing.r.filter;

import com.google.common.hash.Funnels;
import org.datayoo.base.types.DataType;
import org.datayoo.base.types.DataTypeName;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnMetadata;
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
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.r.filter.BloomFilterDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.nio.charset.Charset;

@OpDefiner(name = "BloomFilter",
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
public class BloomFilter extends AbstractResDataDependencyOperator {

  private String srcColumn;
  private String dataColumn;
  private int dataColumnIndex = -1;
  private double fpp;
  private String charset;

  private com.google.common.hash.BloomFilter bloomFilter;

  public BloomFilter(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected void initializeResource(PlRowSet plRowSet) {
    int srcColumnIndex = plRowSet.getColumnSetMetadata()
        .getColumnIndex(srcColumn);
    ColumnMetadata columnMetadata = plRowSet.getColumnSetMetadata()
        .getColumn(srcColumn);
    DataType dataType = columnMetadata.getType();
    if (dataType.getName().equals(DataTypeName.Integer)) {
      bloomFilter = com.google.common.hash.BloomFilter.create(
          Funnels.integerFunnel(), plRowSet.getRowsCount(), fpp);
      for (Object[] row : plRowSet.getRows()) {
        if(row[srcColumnIndex] == null){
          continue;
        }
        bloomFilter.put(row[srcColumnIndex]);
      }
    } else if (dataType.getName().equals(DataTypeName.String)) {
      bloomFilter = com.google.common.hash.BloomFilter.create(
          Funnels.stringFunnel(Charset.forName(charset)),
          plRowSet.getRowsCount(), fpp);
      for (Object[] row : plRowSet.getRows()) {
        if(row[srcColumnIndex] == null){
          continue;
        }
        bloomFilter.put(row[srcColumnIndex]);
      }
    } else if (dataType.getName().equals(DataTypeName.Long)) {
      bloomFilter = com.google.common.hash.BloomFilter.create(
          Funnels.longFunnel(), plRowSet.getRowsCount(), fpp);
      for (Object[] row : plRowSet.getRows()) {
        if(row[srcColumnIndex] == null){
          continue;
        }
        bloomFilter.put(row[srcColumnIndex]);
      }
    } else {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_COL_INVALID_TYPE, this.alias,
          this.parameters.getParameterValue(
              BloomFilterDescriptor.PARAM_SRC_COLUMN), "String,Long,Integer"));
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
    if (dataColumnIndex == -1) {
      dataColumnIndex = columnSetMetadata.getColumnIndex(dataColumn);
    }
    if(row[dataColumnIndex] == null){
      return null;
    }
    if (bloomFilter.mightContain(row[dataColumnIndex])) {
      return row;
    }
    return null;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    srcColumn = groupParameter.getParameterValue(
        BloomFilterDescriptor.PARAM_SRC_COLUMN);
    dataColumn = groupParameter.getParameterValue(
        BloomFilterDescriptor.PARAM_COLUMN);
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
