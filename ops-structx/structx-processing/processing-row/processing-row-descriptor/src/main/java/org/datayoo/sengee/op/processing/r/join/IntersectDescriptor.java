package org.datayoo.sengee.op.processing.r.join;

import org.datayoo.datax.sd.ColumnMetadata;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opd.CompliationCsmDependent;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.List;

/**
 * 交集
 *
 * @author hhn
 */
@OpDefiner(name = "Intersect",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = { @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,join")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_LEFT_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false), @Port(name = SengeeOperatorConstants.PORT_RIGHT_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "</parametergroup>" + "</parameters>",
    compoxes = {})
public class IntersectDescriptor extends AbstractProcessingDescriptor
    implements CompliationCsmDependent {

  public IntersectDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    ColumnSetMetadata leftMetadata = new GeneralColumnSetMetadata(
        this.columnSetMetadataMap.get(SengeeOperatorConstants.PORT_LEFT_IN));
    return leftMetadata;
  }

  //重载，AbstractProcessingDescriptor默认读取columnSet
  @Override
  protected void readParameters() {
  }

  @Override
  protected void validateParameters() {
    ColumnSetMetadata left = this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_LEFT_IN);
    ColumnSetMetadata right = this.columnSetMetadataMap.get(
        SengeeOperatorConstants.PORT_RIGHT_IN);
    if (null != left && null != right) {
      List<ColumnMetadata> leftColumns = left.getColumns();
      List<ColumnMetadata> rightColumns = right.getColumns();
      if (leftColumns.size() > rightColumns.size()) {
        throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
            OperatorsExceptionConstants.ECM_OPD_DIFF_COL_NUM, this.alias));
      }
      int i = 0;
      for (ColumnMetadata leftMetadata : leftColumns) {
        ColumnMetadata rightMetadata = rightColumns.get(i);
        if (!leftMetadata.getType().equals(rightMetadata.getType())) {
          throw new OperationRuntimeException(
              OperatorsI18nMessageResource.format(
                  OperatorsExceptionConstants.ECM_OPD_UNMATCHED_DATATYPE,
                  this.alias, leftMetadata.getName(), rightMetadata.getName()));
        }
        i++;
      }
    }

  }
}
