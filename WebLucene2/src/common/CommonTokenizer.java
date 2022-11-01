package common;


import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;


public class CommonTokenizer extends Tokenizer {
  /** 构造一个例子基于JFlex scanner*/
  private final CommonTokenizerImpl scanner;

  public static final int ALPHANUM          = 0;
  public static final int APOSTROPHE        = 1;
  public static final int ACRONYM           = 2;
  public static final int COMPANY           = 3;
  public static final int EMAIL             = 4;
  public static final int HOST              = 5;
  public static final int NUM               = 6;
  public static final int CJ                = 7;

  /**
   * @deprecated 这解决了以“.”结尾的主机被标识为首字母缩略词的错误。它已被弃用，将在下一个版本中删除,但是在2.9这个版本中现需要处理
   */
  public static final int ACRONYM_DEP       = 8;

  /** 和标记类型int常量相对应的字符串标记类型 */
  public static final String [] TOKEN_TYPES = new String [] {
    "<ALPHANUM>",
    "<APOSTROPHE>",
    "<ACRONYM>",
    "<COMPANY>",
    "<EMAIL>",
    "<HOST>",
    "<NUM>",
    "<CJ>",
    "<ACRONYM_DEP>"
  };

  public static final String [] tokenImage = TOKEN_TYPES;

  private boolean replaceInvalidAcronym;
    
  private int maxTokenLength = StandardAnalyzer.DEFAULT_MAX_TOKEN_LENGTH;

  /** 设置允许的最大切分长度。超过此值的任何词都将被跳过。 */
  public void setMaxTokenLength(int length) {
    this.maxTokenLength = length;
  }

  /** 返回最大的token长度 */
  public int getMaxTokenLength() {
    return maxTokenLength;
  }

  /**
   * 创建一个CommonTokenizer新的实例{@link CommonTokenizer(Version,
   * Reader)}. 
   */
  public CommonTokenizer(Reader input) {
    this(Version.LUCENE_24, input);
  }

  /**
   * 创建一个{@link org.apache.lucene.analysis.standard.CommonTokenizer(Version, Reader)}新的实例.  
   */
  public CommonTokenizer(Reader input, boolean replaceInvalidAcronym) {
    super();
    this.scanner = new CommonTokenizerImpl(input);
    init(input, replaceInvalidAcronym);
  }

  /**
   * 创建一个{@link org.apache.lucene.analysis.standard.CommonTokenizer}新的实例
   */
  public CommonTokenizer(Version matchVersion, Reader input) {
    super();
    this.scanner = new CommonTokenizerImpl(input);
    init(input, matchVersion);
  }

  /**
   * 创建一个新的 CommonTokenizer基于给定的{@link AttributeSource}. 
   */
  public CommonTokenizer(AttributeSource source, Reader input, boolean replaceInvalidAcronym) {
    super(source);
    this.scanner = new CommonTokenizerImpl(input);
    init(input, replaceInvalidAcronym);
  }

  /**
   * Creates a new CommonTokenizer with a given {@link AttributeSource}. 
   */
  public CommonTokenizer(Version matchVersion, AttributeSource source, Reader input) {
    super(source);
    this.scanner = new CommonTokenizerImpl(input);
    init(input, matchVersion);
  }

  /**
   * Creates a new CommonTokenizer with a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory} 
   *
   * @deprecated Use {@link #CommonTokenizer(Version, org.apache.lucene.util.AttributeSource.AttributeFactory, Reader)} instead
   */
  public CommonTokenizer(AttributeFactory factory, Reader input, boolean replaceInvalidAcronym) {
    super(factory);
    this.scanner = new CommonTokenizerImpl(input);
    init(input, replaceInvalidAcronym);
  }

  /**
   * Creates a new CommonTokenizer with a given {@link org.apache.lucene.util.AttributeSource.AttributeFactory} 
   */
  public CommonTokenizer(Version matchVersion, AttributeFactory factory, Reader input) {
    super(factory);
    this.scanner = new CommonTokenizerImpl(input);
    init(input, matchVersion);
  }

  private void init(Reader input, boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
    this.input = input;    
    termAtt = (TermAttribute) addAttribute(TermAttribute.class);
    offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
    posIncrAtt = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
    typeAtt = (TypeAttribute) addAttribute(TypeAttribute.class);
  }

  private void init(Reader input, Version matchVersion) {
    if (matchVersion.onOrAfter(Version.LUCENE_24)) {
      init(input, true);
    } else {
      init(input, false);
    }
  }
  
  private TermAttribute termAtt;
  private OffsetAttribute offsetAtt;
  private PositionIncrementAttribute posIncrAtt;
  private TypeAttribute typeAtt;

  public final boolean incrementToken() throws IOException {
    clearAttributes();
    int posIncr = 1;

    while(true) {
      int tokenType = scanner.getNextToken();

      if (tokenType == CommonTokenizerImpl.YYEOF) {
        return false;
      }

      if (scanner.yylength() <= maxTokenLength) {
        posIncrAtt.setPositionIncrement(posIncr);
        scanner.getText(termAtt);
        final int start = scanner.yychar();
        offsetAtt.setOffset(correctOffset(start), correctOffset(start+termAtt.termLength()));
        if (tokenType == CommonTokenizerImpl.ACRONYM_DEP) {
          if (replaceInvalidAcronym) {
            typeAtt.setType(CommonTokenizerImpl.TOKEN_TYPES[CommonTokenizerImpl.HOST]);
            termAtt.setTermLength(termAtt.termLength() - 1); // remove extra '.'
          } else {
            typeAtt.setType(CommonTokenizerImpl.TOKEN_TYPES[CommonTokenizerImpl.ACRONYM]);
          }
        } else {
          typeAtt.setType(CommonTokenizerImpl.TOKEN_TYPES[tokenType]);
        }
        return true;
      } else
    	 //当我们跳过一个太长的周期，我们仍然向后移动一位
        posIncr++;
    }
  }
  
  public final void end() {
    // 设置最终偏移量
    int finalOffset = correctOffset(scanner.yychar() + scanner.yylength());
    offsetAtt.setOffset(finalOffset, finalOffset);
  }

  /** @deprecated Will be removed in Lucene 3.0. This method is final, as it should
   * not be overridden. Delegates to the backwards compatibility layer. */
  public final Token next(final Token reusableToken) throws IOException {
    return super.next(reusableToken);
  }

  /** @deprecated Will be removed in Lucene 3.0. This method is final, as it should
   * not be overridden. Delegates to the backwards compatibility layer. */
  public final Token next() throws IOException {
    return super.next();
  }


  public void reset() throws IOException {
    super.reset();
    scanner.yyreset(input);
  }

  public void reset(Reader reader) throws IOException {
    super.reset(reader);
    reset();
  }


  public boolean isReplaceInvalidAcronym() {
    return replaceInvalidAcronym;
  }

  /**
   *
   * @param replaceInvalidAcronym Set to true to replace mischaracterized acronyms as HOST.
   * @deprecated Remove in 3.X and make true the only valid value
   */
  public void setReplaceInvalidAcronym(boolean replaceInvalidAcronym) {
    this.replaceInvalidAcronym = replaceInvalidAcronym;
  }
}
