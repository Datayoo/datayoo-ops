package org.datayoo.oyez.op.processing.flat;

import org.datayoo.base.lang.Pair;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.listener.DataListener;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.GeneralPlRowSet;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.EntityMap;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.datax.flatter.StructureCollectionMetadata;
import org.datayoo.sengee.datax.flatter.StructureDataFlatter;
import org.datayoo.sengee.exception.OperationInterruptionException;
import org.datayoo.sengee.op.processing.flat.AbstractFlatterDescriptor;
import org.datayoo.sengee.op.reader.stream.AbstractSemiStructedReaderDescriptor;

import java.util.List;

/**
 *
 */
public abstract class AbstractFlatter extends BaseProcessOperator {
  protected String dataColumn;

  protected int dataColumnIndex = -1;

  protected StructureCollectionMetadata structureCollectionMetadata;

  protected StructureDataFlatter dataFlatter;

  public AbstractFlatter(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(flowNodeMetadata, parent, engineContext);
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    dataColumn = groupParameter.getParameterValue(
        AbstractFlatterDescriptor.PARAM_DATA_COLUMN);
    structureCollectionMetadata = AbstractSemiStructedReaderDescriptor.loadStructureMappings(
        (GroupParameter) parameters.getParameter(
            AbstractFlatterDescriptor.PARAM_STRUCT_MAPPINGS));
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    columnSetMetadata = new GeneralColumnSetMetadata(
        outputPort.getFlowDataType());
    columnSetMetadata.getColumns().addAll(
        AbstractFlatterDescriptor.trans2ColumnMetadatas(
            structureCollectionMetadata));
    return columnSetMetadata;
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {
    if (dataFlatter != null)
      return;
    dataFlatter = createDataFlatter();
  }

  @Override
  protected void operatorDestroy() {
    dataFlatter = null;
  }

  protected void innerOperate() {
    PlRowSet rowSet = inputPort.read();
    int i = 0;
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    preBindColumnSetMetadata(columnSetMetadata);
    for (Object[] row : rowSet.getRows()) {
      if (getEngineContext().isTermination())
        return;
      try {
        innerOperate(columnSetMetadata, i++, row);
      } catch (Throwable t) {
        if (!reportErrorAndRun(row, t)) {
          throw new OperationInterruptionException(t);
        }
      }
    }
  }

  @Override
  protected void preBindColumnSetMetadata(ColumnSetMetadata columnSetMetadata) {
    if (dataColumnIndex == -1) {
      dataColumnIndex = columnSetMetadata.getColumnIndex(dataColumn);
    }
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    Object data = objects[dataColumnIndex];
    if (data == null)
      return null;
    InnerDataListener dataListener = new InnerDataListener(
        getEngineContext().getRowBatchSize(),
        createOutputColumnSetMetadata(columnSetMetadata));
    dataFlatter.setDataListener(dataListener);
    EntityMap entityMap = loadData(data);
    if (entityMap == null)
      return null;
    dataFlatter.structure(entityMap);
    dataListener.flush();
    return null;
  }

  protected abstract EntityMap loadData(Object data);

  protected abstract StructureDataFlatter createDataFlatter();

  protected class InnerDataListener
      implements DataListener<Pair<String, Object>> {

    protected int size = 0;

    protected int batchSize = 0;

    protected ColumnSetMetadata columnSetMetadata;

    protected PlRowSet plRowSet;

    public InnerDataListener(int batchSize,
        ColumnSetMetadata columnSetMetadata) {
      this.batchSize = batchSize;
      this.columnSetMetadata = columnSetMetadata;
    }

    @Override
    public void onData(Pair<String, Object> stringObjectPair) {
      throw new UnsupportedOperationException("");
    }

    @Override
    public void onData(List<Pair<String, Object>> list) {
      if (plRowSet == null) {
        plRowSet = new GeneralPlRowSet(AbstractFlatter.this.name,
            columnSetMetadata);
      }
      Object[] row = new Object[plRowSet.getColumnSetMetadata().getColumns()
          .size()];
      int i = 0;
      for (Pair<String, Object> pair : list) {
        row[i++] = pair.getValue();
      }
      plRowSet.addRow(row);
      if (plRowSet.getRowsCount() == batchSize) {
        if (getFrom().size() == 0) {
          outputPort.write(plRowSet);
        } else {
          outputPort.write(plRowSet, inputPort.getWaterMark());
        }
        plRowSet = null;
      }
      size++;
    }

    public void flush() {
      if (plRowSet != null) {
        if (getFrom().size() == 0) {
          outputPort.write(plRowSet);
        } else {
          outputPort.write(plRowSet, inputPort.getWaterMark());
        }
        plRowSet = null;
      }
    }

    public int getSize() {
      return size;
    }
  }

}
