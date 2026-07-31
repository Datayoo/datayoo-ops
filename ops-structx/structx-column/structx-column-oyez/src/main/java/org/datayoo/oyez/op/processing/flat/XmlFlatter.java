package org.datayoo.oyez.op.processing.flat;

import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.moql.EntityMap;
import org.datayoo.moql.EntityMapImpl;
import org.datayoo.moql.MoqlException;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.datax.flatter.StructureDataFlatter;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.exception.OperatorsExceptionConstants;
import org.datayoo.sengee.op.util.MoqlExceptionHelper;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.ByteArrayInputStream;

@OpDefiner(name = "XmlFlatter",
    type = OperatorProfileConstants.OC_PROCESS,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    inputPorts = { @Port(name = "dataIn",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    outputPorts = { @Port(name = "dataOut",
        flowDataType = SengeeConstants.FDT_ANY,
        option = false)
    },
    parameters = "<parameters></parameters>",
    compoxes = {})
public class XmlFlatter extends AbstractFlatter {

  public XmlFlatter(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(flowNodeMetadata, parent, engineContext);
  }

  @Override
  protected StructureDataFlatter createDataFlatter() {
    try {
      return new org.datayoo.sengee.datax.flatter.xml.XmlFlatter(
          this.structureCollectionMetadata);
    } catch (MoqlException e) {
      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
          operand));
    }
  }

  @Override
  protected EntityMap loadData(Object data) {
    SAXReader reader = new SAXReader();
    Document document = null;
    try {
      document = reader.read(new ByteArrayInputStream(((String)data).getBytes()));
    } catch (DocumentException e) {
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_STREAM_READ_FAILED, this.alias), e);
    }
    EntityMapImpl entityMap = new EntityMapImpl();
    entityMap.putEntity(document.getRootElement().getName(),
        document.getRootElement());
    return entityMap;
  }
}
