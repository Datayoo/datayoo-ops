package org.datayoo.oyez.op.processing.stream.extractor;

import org.apache.avro.file.DataFileStream;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.datayoo.configx.parameter.GroupParameter;
import org.datayoo.datax.sd.ColumnSetMetadata;
import org.datayoo.datax.sd.PlRowSet;
import org.datayoo.flowx.annotation.Port;
import org.datayoo.flowx.metadata.FlowNodeMetadata;
import org.datayoo.flowx.node.FlowNode;
import org.datayoo.flowx.node.FlowPort;
import org.datayoo.oyez.op.BaseProcessOperator;
import org.datayoo.oyez.op.EngineContext;
import org.datayoo.oyez.port.OyezInputPort;
import org.datayoo.oyez.port.OyezOutputPort;
import org.datayoo.sengee.SengeeConstants;
import org.datayoo.sengee.annotation.OpDefiner;
import org.datayoo.sengee.exception.OperationRuntimeException;
import org.datayoo.sengee.op.SengeeOperatorConstants;
import org.datayoo.sengee.op.util.ColumnSetMetadataLibrary;
import org.datayoo.sengee.opp.OperatorProfileConstants;
import org.datayoo.util.cntr.DependencyPolicy;
import org.datayoo.util.io.InputStreamUtils;
import org.xml.sax.ContentHandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

@OpDefiner(name = "TextExtractor",
    type = OperatorProfileConstants.OC_READER,
    version = "1.0",
    portrait = "",
    computionFramework = "oyez",
    dependencyPolicy = DependencyPolicy.ConflictIsolate,
    inputPorts = { @Port(name = SengeeOperatorConstants.PORT_STREAM_IN,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = false)
    },
    outputPorts = { @Port(name = SengeeOperatorConstants.PORT_BLOCK_OUT,
        flowDataType = SengeeConstants.FDT_STREAM_BLOCK,
        option = true), @Port(name = SengeeOperatorConstants.PORT_STREAM_OUT,
        flowDataType = SengeeConstants.FDT_DATA_STREAM,
        option = true)
    },
    parameters = "",
    compoxes = {})
public class TextExtractor extends BaseProcessOperator {

  public static char[] escapeChars = new char[] { '\r', '\n', '\t' };
  public static char[] escapedChars = new char[] { 'r', 'n', 't' };

  public static final String PARAM_TEXT_LIMIT = "textLimit";
  public static final String PARAM_IGNORE_PAGENO = "ignorePageNo";
  public static final String PARAM_IGNORE_SUPERSCRIPT = "ignoreSuperscript";
  public static final String PARAM_IGNORE_LINK = "ignoreLink";
  public static final String PARAM_IGNORE_EMBEDED_FILE_NAME = "ignoreEmbededFileName";

  protected AutoDetectParser parser;

  protected OyezOutputPort streamOutputPort;
  //  protected OyezOutputPort blockOutputPort;

  protected int textLimit = 100000;
  protected boolean ignorePageNo = false;
  protected boolean ignoreSuperscript = false;
  protected boolean ignoreLink = false;
  protected boolean ignoreEmbededFileName = false;

  public TextExtractor(FlowNodeMetadata operatorMetadata, FlowNode parent,
      EngineContext engineContext) {
    super(operatorMetadata, parent, engineContext);
  }

  @Override
  protected ColumnSetMetadata createOutputColumnSetMetadata(
      ColumnSetMetadata columnSetMetadata) {
    return ColumnSetMetadataLibrary.createBlockMetadata(false);
  }

  @Override
  protected void innerOperate() {
    PlRowSet rowSet = this.inputPort.read();
    int i = 0;
    ColumnSetMetadata columnSetMetadata = rowSet.getColumnSetMetadata();
    for (Object[] row : rowSet.getRows()) {
      if (this.getEngineContext().isTermination()) {
        return;
      }
      if (!isAvro((String) row[0])) {
        this.innerOperate(columnSetMetadata, i++, row);
      } else {
        extractAvro(columnSetMetadata, row);
      }
    }
  }

  protected boolean isAvro(String name) {
    if (name.endsWith(".avro")) {
      return true;
    }
    return false;
  }

  protected void extractAvro(ColumnSetMetadata columnSetMetadata,
      Object[] objects) {
    InputStream is = (InputStream) objects[1];
    DatumReader<GenericRecord> datumReader = new GenericDatumReader<>();
    try {
      DataFileStream<GenericRecord> dataFileStream = new DataFileStream(is,
          datumReader);
      StringBuilder content = new StringBuilder();
      while (dataFileStream.hasNext()) {
        GenericRecord record = dataFileStream.next();
        content.append(record.toString()).append("\n");
      }
      writeOut(objects, content.toString());
    } catch (IOException e) {
      throw new OperationRuntimeException(
          String.format("read file '%s' failed!", (String) objects[0]));
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  protected void writeOut(Object[] row, String data) {
    ColumnSetMetadata blockMetadata = ColumnSetMetadataLibrary.createBlockMetadata(
        false);
    PlRowSet outSet = this.createRowSet(blockMetadata);
    Object[] result = new Object[blockMetadata.getColumns().size()];
    System.arraycopy(row, 0, result, 0, result.length);
    result[1] = data.trim();
    outSet.addRow(result);
    this.outputPort.write(outSet, this.inputPort.getWaterMark());
  }

  @Override
  protected Object[] innerOperate(ColumnSetMetadata columnSetMetadata, int i,
      Object[] objects) {
    InputStream inputStream = (InputStream) objects[1];
    Metadata metadata = new Metadata();
    boolean closeStream = true;
    try {
      metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, (String) objects[0]);
      ContentHandler handler = buildContentHandler(metadata);
      parser.parse(inputStream, handler, metadata);
      String data = handler.toString();
      if (StringUtils.isEmpty(data.trim())) {
        PlRowSet outSet = this.createRowSet(columnSetMetadata);
        InputStreamUtils.reset(inputStream);
        closeStream = false;
        outSet.addRow(objects);
        this.streamOutputPort.write(outSet, this.inputPort.getWaterMark());
      } else {
        writeOut(objects, data);
      }
    } catch (Throwable t) {
      throw new OperationRuntimeException(
          String.format("Create stream '%s' failed!", objects[0]), t);
    } finally {
      if (inputStream != null && closeStream) {
        try {
          inputStream.close();
        } catch (IOException ex) {
        }
      }
    }
    return objects;
  }

  protected static String escapeString(String v) {
    StringBuffer sbuf = new StringBuffer();
    for (int i = 0; i < v.length(); i++) {
      char ch = v.charAt(i);
      char c = transEscapeChar(ch);
      if (c != 0) {
        continue;
      }
      sbuf.append(ch);
    }
    return sbuf.toString();
  }

  protected static char transEscapeChar(char ch) {
    for (int i = 0; i < escapeChars.length; i++) {
      if (ch == escapeChars[i])
        return escapedChars[i];
    }
    return 0;
  }

  @Override
  protected void presetAttributes(GroupParameter groupParameter) {
    textLimit = this.parameters.getParameterValueAsInt(PARAM_TEXT_LIMIT,
        100000);
    ignorePageNo = this.parameters.getParameterValueAsBoolean(
        PARAM_IGNORE_PAGENO, false);
    ignoreSuperscript = this.parameters.getParameterValueAsBoolean(
        PARAM_IGNORE_SUPERSCRIPT, false);
    ignoreLink = this.parameters.getParameterValueAsBoolean(PARAM_IGNORE_LINK,
        false);
    ignoreEmbededFileName = this.parameters.getParameterValueAsBoolean(
        PARAM_IGNORE_EMBEDED_FILE_NAME, false);
  }

  @Override
  protected void buildFeatureStates() {

  }

  @Override
  protected void operatorInitialize() {
    try {
      parser = new AutoDetectParser(
          new TikaConfig(this.getClass().getClassLoader()));
    } catch (MimeTypeException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected ContentHandler buildContentHandler(Metadata metadata) {
    ConfigurableBodyContentHandler handler = new ConfigurableBodyContentHandler(
        textLimit, metadata);
    if (ignorePageNo) {
      handler.setIgnorePageNo(ignorePageNo);
    }
    if (ignoreSuperscript) {
      handler.setIgnoreSuperscript(ignoreSuperscript);
    }
    if (ignoreLink) {
      handler.addIgnoredElement("a");
    }
    if (ignoreEmbededFileName) {
      handler.setIgnoreEmbededFileName(ignoreEmbededFileName);
    }
    return handler;
  }

  @Override
  protected void operatorDestroy() {

  }

  @Override
  protected void checkPorts() {
    if (this.inputPorts.size() != 1) {
      throw new IllegalArgumentException(
          String.format("Operator '%s' should has only 1 input port!",
              this.alias));
    } else {
      this.inputPort = (OyezInputPort) this.inputPorts.iterator().next();
      if (this.outputPorts.size() != 2) {
        throw new IllegalArgumentException(
            String.format("Operator '%s' should has only 2 output port!",
                this.alias));
      } else {
        this.outputPort = (OyezOutputPort) getInportByName(
            SengeeOperatorConstants.PORT_BLOCK_OUT);
        this.streamOutputPort = (OyezOutputPort) getInportByName(
            SengeeOperatorConstants.PORT_STREAM_OUT);
      }
    }
  }

  protected FlowPort getInportByName(String name) {
    Iterator var2 = this.outputPorts.iterator();
    while (var2.hasNext()) {
      FlowPort port = (FlowPort) var2.next();
      if (port.getName().equals(name)) {
        return port;
      }
    }
    return null;
  }
}
