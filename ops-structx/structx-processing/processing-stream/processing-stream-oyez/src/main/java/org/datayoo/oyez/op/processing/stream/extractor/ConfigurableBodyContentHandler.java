package org.datayoo.oyez.op.processing.stream.extractor;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.OutputStream;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class ConfigurableBodyContentHandler extends BodyContentHandler {

  protected static Set<String> paragraphElements = new HashSet<>();

  static {
    paragraphElements.add("p");
  }

  protected Metadata metadata;

  protected Set<String> ignoredElements = new HashSet<>();
  protected Set<String> warnnedElements = new HashSet<>();

  protected boolean ignoreSuperscript = false;
  protected boolean ignorePageNo = false;

  protected boolean ignoreEmbededFileName = false;

  // 该实现只能过滤掉最后一层element
  protected boolean ignored = false;
  protected boolean warnned = false;
  protected boolean ignorePageWorked = false;

  protected boolean merged = false;

  protected int start = -1;
  protected int length;
  protected StringBuilder sbud = new StringBuilder();

  protected boolean ignoreWhiteSpace = false;

  public ConfigurableBodyContentHandler(ContentHandler handler,
      Metadata metadata) {
    super(handler);
    if (metadata == null)
      throw new NullPointerException("metadata is null!");
    this.metadata = metadata;
  }

  public ConfigurableBodyContentHandler(Writer writer, Metadata metadata) {
    super(writer);
    if (metadata == null)
      throw new NullPointerException("metadata is null!");
    this.metadata = metadata;
  }

  public ConfigurableBodyContentHandler(OutputStream stream,
      Metadata metadata) {
    super(stream);
    if (metadata == null)
      throw new NullPointerException("metadata is null!");
    this.metadata = metadata;
  }

  public ConfigurableBodyContentHandler(int writeLimit, Metadata metadata) {
    super(writeLimit);
    if (metadata == null)
      throw new NullPointerException("metadata is null!");
    this.metadata = metadata;
  }

  public ConfigurableBodyContentHandler(Metadata metadata) {
    super();
    if (metadata == null)
      throw new NullPointerException("metadata is null!");
    this.metadata = metadata;
  }

  protected boolean pageWorked() {
    String mimeType = metadata.get("Content-Type");
    switch (mimeType) {
      case "application/pdf":
      case "application/acrobat":
      case "application/nappdf":
      case "application/x-pdf":
        return true;
    }
    return false;
  }

  public void addIgnoredElement(String element) {
    ignoredElements.add(element);
  }

  public void removeIgnoredElement(String element) {
    ignoredElements.remove(element);
  }

  public boolean isIgnoreSuperscript() {
    return ignoreSuperscript;
  }

  public void setIgnoreSuperscript(boolean ignoreSuperscript) {
    this.ignoreSuperscript = ignoreSuperscript;
  }

  public boolean isIgnorePageNo() {
    return ignorePageNo;
  }

  public void setIgnorePageNo(boolean ignorePageNo) {
    this.ignorePageNo = ignorePageNo;
    this.ignorePageWorked = ignorePageNo;
  }

  public boolean isIgnoreEmbededFileName() {
    return ignoreEmbededFileName;
  }

  public void setIgnoreEmbededFileName(boolean ignoreEmbededFileName) {
    this.ignoreEmbededFileName = ignoreEmbededFileName;
    warnnedElements.add("h1");
  }

  @Override
  public void startElement(String uri, String localName, String name,
      Attributes atts) throws SAXException {
    super.startElement(uri, localName, name, atts);
    if (ignorePageNo && ignorePageWorked) {
      ignorePageWorked = pageWorked();
    }
    if (paragraphElements.contains(name)) {
      if (sbud.length() > 0) {
        ignoreWhiteSpace = false;
        super.characters(sbud.toString().toCharArray(), start, length);
        sbud = new StringBuilder();
        start = -1;
        length = 0;
        merged = false;
      }
      merged = true;
    } else if (ignoredElements.contains(name))
      ignored = true;
    else if (warnnedElements.contains(name))
      warnned = true;
  }

  @Override
  public void endElement(String uri, String localName, String name)
      throws SAXException {
    super.endElement(uri, localName, name);
    if (paragraphElements.contains(name)) {
      ignoreWhiteSpace = false;
      if (sbud.length() == 0)
        return;
      super.characters(sbud.toString().toCharArray(), start, length);
      sbud = new StringBuilder();
      start = -1;
      length = 0;
      merged = false;
    } else if (ignoredElements.contains(name))
      ignored = false;
    else if (warnnedElements.contains(name))
      warnned = false;
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    try {
      ignoreWhiteSpace = true;
      if (ignored)
        return;
      if (ch.length == 0)
        return;
      if (ch.length == 1 && ch[0] == ' ') {
        return;
      }
      if (warnned) {
        if (isIgnored(ch))
          return;
      }
      if (ch[0] == '[' && ignoreSuperscript) {
        if (isSupperscript(ch))
          return;
      }
      if (isPageNo(ch) && ignorePageWorked) {
        return;
      }
      if (merged) {
        sbud.append(ch);
        if (this.start == -1) {
          this.start = start;
        }
        this.length += length;
        return;
      }
      ignoreWhiteSpace = false;
      super.characters(ch, start, length);
    } catch (SAXException var5) {
      this.handleException(var5);
    }
  }

  @Override
  public void ignorableWhitespace(char[] ch, int start, int length)
      throws SAXException {
    if (ignoreWhiteSpace)
      return;
    super.ignorableWhitespace(ch, start, length);
  }

  protected boolean isSupperscript(char[] chars) {
    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];
      if (ch == '[' || ch == ']')
        continue;
      if (Character.isDigit(ch))
        continue;
      return false;
    }
    return true;
  }

  protected boolean isPageNo(char[] chars) {
    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];
      if (ch == '/')
        continue;
      if (Character.isDigit(ch))
        continue;
      return false;
    }
    return true;
  }

  protected boolean isIgnored(char[] chars) {
    if (isEmbededFileName(chars))
      return true;
    return false;
  }

  protected boolean isEmbededFileName(char[] chars) {
    boolean hasDot = false;
    int lastDotIndex = 0;
    for (int i = 0; i < chars.length; i++) {
      char ch = chars[i];
      if (ch == '/' || ch == '\\')
        return false;
      if (ch == '.') {
        hasDot = true;
        lastDotIndex = i;
      }
    }
    if (hasDot && chars.length - lastDotIndex < 8) {
      return true;
    }
    return false;
  }

}
