package org.datayoo.oyez.op.processing.flat;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.datayoo.configx.parameter.GroupParameter;
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
import org.datayoo.sengee.op.processing.flat.JsonFlatterDescriptor;
import org.datayoo.sengee.op.reader.stream.AbstractSemiStructedReaderDescriptor;
import org.datayoo.sengee.op.util.MoqlExceptionHelper;
import org.datayoo.sengee.op.util.OperatorsI18nMessageResource;
import org.datayoo.sengee.opp.OperatorProfileConstants;

import java.io.StringReader;

@OpDefiner(name = "JsonFlatter",
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
public class JsonFlatter extends AbstractFlatter {

  protected boolean lenient = false;

  public JsonFlatter(FlowNodeMetadata flowNodeMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(flowNodeMetadata, parent, engineContext);
  }

  @Override
  protected StructureDataFlatter createDataFlatter() {
    try {
      return new org.datayoo.sengee.datax.flatter.json.JsonFlatter(
          this.structureCollectionMetadata);
    } catch (MoqlException e) {
      String operand = MoqlExceptionHelper.extractOperand(e.getMessage());
      throw new OperationRuntimeException(OperatorsI18nMessageResource.format(
          OperatorsExceptionConstants.ECM_OPD_OPERAND_BUILD_FAILED, this.alias,
          operand));
    }
  }

  protected EntityMap loadData(Object data) {
    EntityMapImpl entityMap = new EntityMapImpl();
    JsonReader jsonReader = new JsonReader(new StringReader((String) data));
    entityMap.putEntity(AbstractSemiStructedReaderDescriptor.OBJECT_ENTITY_NAME,
        JsonParser.parseReader(jsonReader));
    return entityMap;
  }

}
