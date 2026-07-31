package org.datayoo.oyez.op.processing.v.transformer;

import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.GeneralPlRowSet;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.lang.util.PropertyPlaceholder;
import org.datayoo.oyez.op.AbstractOyezOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.port.OyezInputPort;
import org.datayoo.oyez.port.OyezOutputPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.processing.v.transformer.ReplaceTextTemplateDescriptor;
import org.datayoo.sengee.op.processing.v.transformer.ValuesReplaceDescriptor;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.util.Map;

@OpDefiner(name = "ReplaceTextTemplate",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    inputPorts = { @Port(name = ReplaceTextTemplateDescriptor.PORT_TEMPLATE,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true), @Port(name = SengeeOperatorConstants.PORT_DATA_IN,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_DATA_OUT,
        flowDataType = SengeeConstants.FDT_ANY,
        option = true)
    },
    parameters = "",
    compoxes = {})
public class ReplaceTextTemplate extends AbstractOyezOperator {

  protected OyezInputPort templateIn;
  protected OyezInputPort inputPort;
  protected OyezOutputPort outputPort;

  private String columnName;
  private int columnIndex;

  private String textTemplate;

  private ColumnSetMetadata outColumnSetMetadaata;

  private Object[] templateRow;

  public ReplaceTextTemplate(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
    checkPorts();
  }

  protected void checkPorts() {
    if (this.inputPorts.size() != 2) {
      throw new IllegalArgumentException(
          String.format("Operator '%s' should has only 2 input port!",
              this.alias));
    } else {
      for (FlowPort<PlRowSet> inputPort : inputPorts) {
        if (inputPort.getName()
            .equals(ReplaceTextTemplateDescriptor.PORT_TEMPLATE)) {
          templateIn = (OyezInputPort) inputPort;
        } else {
          this.inputPort = (OyezInputPort) inputPort;
        }
      }
      if (this.outputPorts.size() != 1) {
        throw new IllegalArgumentException(
            String.format("Operator '%s' should has only 1 output port!",
                this.alias));
      } else {
        this.outputPort = (OyezOutputPort) this.outputPorts.iterator().next();
      }
    }
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    columnName = parameters.getParameterValue(
        ValuesReplaceDescriptor.PARAM_COLUMN_NAME);
  }

  @Override
  protected void operatorInitialize() {

  }

  @Override
  protected void operatorDestroy() {

  }

  @Override
  protected boolean isReady() {
    if (templateIn.isReady())
      return true;
    if (inputPort.isReady())
      return true;
    return false;
  }

  @Override
  protected void innerOperate() {
    if (templateIn.isReady()) {
      readTemplate();
    }
    if (textTemplate != null && inputPort.isReady()) {
      replaceText();
    }
  }

  protected void readTemplate() {
    PlRowSet plRowSet = templateIn.read();
    if (plRowSet.getRowsCount() == 0) {
      return;
    }
    outColumnSetMetadaata = plRowSet.getColumnSetMetadata();
    columnIndex = outColumnSetMetadaata.getColumnIndex(columnName);
    templateRow = plRowSet.getRow(0);
    textTemplate = (String) templateRow[columnIndex];
  }

  protected void replaceText() {
    PlRowSet plRowSet = inputPort.read();
    PlRowSet outSet = new GeneralPlRowSet(outputPort.getName(),
        outColumnSetMetadaata);
    for (Map<String, Object> map : plRowSet.getRowsAsMaps()) {
      Object[] row = new Object[templateRow.length];
      System.arraycopy(templateRow, 0, row, 0, row.length);
      String text = PropertyPlaceholder.resolvePlaceholders(textTemplate, map,
          SengeeConstants.RUNTIME_PLACEHOLDER_PREFIX,
          SengeeConstants.RUNTIME_PLACEHOLDER_SUFFIX, true);
      row[columnIndex] = text;
      outSet.addRow(row);
    }
    outputPort.write(outSet, inputPort.getWaterMark());
  }
}
