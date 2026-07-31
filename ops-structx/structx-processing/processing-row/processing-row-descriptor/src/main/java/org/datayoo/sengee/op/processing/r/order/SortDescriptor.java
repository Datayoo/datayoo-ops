package org.datayoo.sengee.op.processing.r.order;

import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.annotation.TagPair;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.footstone.sightx.annotation.Compox;
import org.datayoo.footstone.sightx.annotation.DataDesc;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.AbstractProcessingDescriptor;
import org.datayoo.sengee.opd.CompilationContext;
import org.datayoo.sengee.opp.OperatorProfileConstants;

/**
 * 排序
 *
 * @author hhn
 */
@OpDefiner(name = "Sort",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    computionFramework = "sengee",
    tags = {
        @TagPair(name = SengeeConstants.TAG_OP_CATS, value = "rowset,order")
    },
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters =
        "<parameters><parametergroup name=\"general\" c_Alias=\"一般\" c_Compox=\"sightx-tab\">"
            + "<parametertable name=\"columnSet\" c_Alias=\"排序属性\"><head>"
            + "<parameter name=\"columnName\" c_Option=\"false\" c_Alias=\"属性名称\" c_Compox=\"sengee-column-selector\"></parameter>"
            + "<parameter name=\"direction\" c_Option=\"false\" c_Alias=\"排序方向\" c_Compox=\"sengee-direct\">DESC</parameter>"
            + "</head>" + "</parametertable></parametergroup></parameters>",
    compoxes = { @Compox(id = "sengee-direct",
        parentId = "sightx-select",
        visibleDescriptors = {},
        dataDescriptor = @DataDesc(defaultValue = "DESC",
            data = "[{\"label\": \"升序\", \"value\": \"ASC\"}, {\"label\": \"降序\", \"value\": \"DESC\"}]"))
    })
public class SortDescriptor extends AbstractProcessingDescriptor {

  public static final String PARAM_DIRECTION = "direction";

  public SortDescriptor(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      CompilationContext compilationContext) {
    super(flowNodeMetadata, parent, compilationContext);
  }

  @Override
  protected ColumnSetMetadata compileCsmOfOutputPort(
      FlowPort<PlRowSet> flowPort) {
    return getInputColumnSetMetadata();
  }

  @Override
  protected void innerInitialize() {

  }

  @Override
  protected void innerDestroy() {

  }
}
