import org.junit.Test;

public class StringTests extends LangTest
{
    /*
     * Tests for strlen.
     */
    @Test
    public void simpleStrLen()
    {
        runTypeTest("simpleStrLen", "strlen(\"hello\");", "int");
        runEvalTest("simpleStrLen", "strlen(\"hello\");", "5");
    }

    @Test
    public void emptyStrLen()
    {
        runTypeTest("emptyStrLen", "strlen(\"\");", "int");
        runEvalTest("emptyStrLen", "strlen(\"\");", "0");
    }

    @Test
    public void strLenAfterStrCat()
    {
        runTypeTest("strLenAfterStrCat", "strlen(strcat(\"a\" \"bc\"));", "int");
        runEvalTest("strLenAfterStrCat", "strlen(strcat(\"a\" \"bc\"));", "3");
    }

    /*
     * Tests for strcat.
     */
    @Test
    public void simpleStrCat()
    {
        runTypeTest("simpleStrCat", "strcat(\"ab\" \"cd\");", "string");
        runEvalTest("simpleStrCat", "strcat(\"ab\" \"cd\");", "abcd");
    }

    @Test
    public void strCatLeftEmpty()
    {
        runTypeTest("strCatLeftEmpty", "strcat(\"\" \"xyz\");", "string");
        runEvalTest("strCatLeftEmpty", "strcat(\"\" \"xyz\");", "xyz");
    }

    @Test
    public void strCatRightEmpty()
    {
        runTypeTest("strCatRightEmpty", "strcat(\"xyz\" \"\");", "string");
        runEvalTest("strCatRightEmpty", "strcat(\"xyz\" \"\");", "xyz");
    }

    /*
     * Tests for substr.
     * substr(s start len)
     */
    @Test
    public void simpleSubstr()
    {
        runTypeTest("simpleSubstr", "substr(\"hello\" 1 3);", "string");
        runEvalTest("simpleSubstr", "substr(\"hello\" 1 3);", "ell");
    }

    @Test
    public void substrWholeString()
    {
        runTypeTest("substrWholeString", "substr(\"hello\" 0 5);", "string");
        runEvalTest("substrWholeString", "substr(\"hello\" 0 5);", "hello");
    }

    @Test
    public void substrAfterStrCat()
    {
        runTypeTest("substrAfterStrCat", "substr(strcat(\"he\" \"llo\") 1 3);", "string");
        runEvalTest("substrAfterStrCat", "substr(strcat(\"he\" \"llo\") 1 3);", "ell");
    }

    /*
     * Tests for explode.
     *
     * IMPORTANT:
     * The expected eval string depends on how your interpreter prints lists/strings.
     * If these fail ONLY on the eval string, paste the "expected/actual" output
     * and I'll adjust to match your printer exactly.
     */
    @Test
    public void simpleExplode()
    {
        runTypeTest("simpleExplode", "explode(\"abc\");", "[ string ]");
        runEvalTest("simpleExplode", "explode(\"abc\");", "[a, b, c]");
    }

    @Test
    public void explodeEmpty()
    {
        runTypeTest("explodeEmpty", "explode(\"\");", "[ string ]");
        runEvalTest("explodeEmpty", "explode(\"\");", "[]");
    }
}
