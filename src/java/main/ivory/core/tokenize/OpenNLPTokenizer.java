package ivory.core.tokenize;

import ivory.core.Constants;
import java.io.IOException;
import java.util.Set;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.tartarus.snowball.SnowballStemmer;
import com.google.common.collect.Sets;
import edu.umd.hooka.VocabularyWritable;
import edu.umd.hooka.alignment.HadoopAlign;

public class OpenNLPTokenizer extends ivory.core.tokenize.Tokenizer {
  private static final Logger sLogger = Logger.getLogger(OpenNLPTokenizer.class);
  static{
    sLogger.setLevel(Level.WARN);
  }
  private Tokenizer tokenizer;
  private SnowballStemmer stemmer;
  private int lang;
  private static final int ENGLISH = 0, FRENCH = 1, GERMAN = 2;
  private static final String[] languages = {"english", "french", "german"};
  private static final String[] TERRIER_STOP_WORDS = {
    "a",
    "abaft",
    "abafter",
    "abaftest",
    "about",
    "abouter",
    "aboutest",
    "above",
    "abover",
    "abovest",
    "accordingly",
    "aer",
    "aest",
    "afore",
    "after",
    "afterer",
    "afterest",
    "afterward",
    "afterwards",
    "again",
    "against",
    "aid",
    "ain",
    "albeit",
    "all",
    "aller",
    "allest",
    "alls",
    "allyou",
    "almost",
    "along",
    "alongside",
    "already",
    "also",
    "although",
    "always",
    "amid",
    "amidst",
    "among",
    "amongst",
    "an",
    "and",
    "andor",
    "anear",
    "anent",
    "another",
    "any",
    "anybody",
    "anyhow",
    "anyone",
    "anything",
    "anywhere",
    "apart",
    "aparter",
    "apartest",
    "appear",
    "appeared",
    "appearing",
    "appears",
    "appropriate",
    "appropriated",
    "appropriater",
    "appropriates",
    "appropriatest",
    "appropriating",
    "are",
    "ares",
    "around",
    "as",
    "ases",
    "aside",
    "asides",
    "aslant",
    "astraddle",
    "astraddler",
    "astraddlest",
    "astride",
    "astrider",
    "astridest",
    "at",
    "athwart",
    "atop",
    "atween",
    "aught",
    "aughts",
    "available",
    "availabler",
    "availablest",
    "awfully",
    "b",
    "be",
    "became",
    "because",
    "become",
    "becomes",
    "becoming",
    "becominger",
    "becomingest",
    "becomings",
    "been",
    "before",
    "beforehand",
    "beforehander",
    "beforehandest",
    "behind",
    "behinds",
    "below",
    "beneath",
    "beside",
    "besides",
    "better",
    "bettered",
    "bettering",
    "betters",
    "between",
    "betwixt",
    "beyond",
    "bist",
    "both",
    "but",
    "buts",
    "by",
    "by-and-by",
    "byandby",
    "c",
    "cannot",
    "canst",
    "cant",
    "canted",
    "cantest",
    "canting",
    "cants",
    "cer",
    "certain",
    "certainer",
    "certainest",
    "cest",
    "chez",
    "circa",
    "co",
    "come-on",
    "come-ons",
    "comeon",
    "comeons",
    "concerning",
    "concerninger",
    "concerningest",
    "consequently",
    "considering",
    "could",
    "couldst",
    "cum",
    "d",
    "dday",
    "ddays",
    "describe",
    "described",
    "describes",
    "describing",
    "despite",
    "despited",
    "despites",
    "despiting",
    "did",
    "different",
    "differenter",
    "differentest",
    "do",
    "doe",
    "does",
    "doing",
    "doings",
    "done",
    "doner",
    "dones",
    "donest",
    "dos",
    "dost",
    "doth",
    "downs",
    "downward",
    "downwarder",
    "downwardest",
    "downwards",
    "during",
    "e",
    "each",
    "eg",
    "eight",
    "either",
    "else",
    "elsewhere",
    "enough",
    "ere",
    "et",
    "etc",
    "even",
    "evened",
    "evenest",
    "evens",
    "evenser",
    "evensest",
    "ever",
    "every",
    "everybody",
    "everyone",
    "everything",
    "everywhere",
    "ex",
    "except",
    "excepted",
    "excepting",
    "excepts",
    "exes",
    "f",
    "fact",
    "facts",
    "failing",
    "failings",
    "few",
    "fewer",
    "fewest",
    "figupon",
    "figuponed",
    "figuponing",
    "figupons",
    "five",
    "followthrough",
    "for",
    "forby",
    "forbye",
    "fore",
    "forer",
    "fores",
    "forever",
    "former",
    "formerer",
    "formerest",
    "formerly",
    "formers",
    "fornenst",
    "forwhy",
    "four",
    "fourscore",
    "frae",
    "from",
    "fs",
    "further",
    "furthered",
    "furtherer",
    "furtherest",
    "furthering",
    "furthermore",
    "furthers",
    "g",
    "get",
    "gets",
    "getting",
    "go",
    "gone",
    "good",
    "got",
    "gotta",
    "gotten",
    "h",
    "had",
    "hadst",
    "hae",
    "hardly",
    "has",
    "hast",
    "hath",
    "have",
    "haves",
    "having",
    "he",
    "hence",
    "her",
    "hereafter",
    "hereafters",
    "hereby",
    "herein",
    "hereupon",
    "hers",
    "herself",
    "him",
    "himself",
    "his",
    "hither",
    "hitherer",
    "hitherest",
    "hoo",
    "hoos",
    "how",
    "how-do-you-do",
    "howbeit",
    "howdoyoudo",
    "however",
    "huh",
    "humph",
    "i",
    "idem",
    "idemer",
    "idemest",
    "ie",
    "if",
    "ifs",
    "immediate",
    "immediately",
    "immediater",
    "immediatest",
    "in",
    "inasmuch",
    "inc",
    "indeed",
    "indicate",
    "indicated",
    "indicates",
    "indicating",
    "info",
    "information",
    "insofar",
    "instead",
    "into",
    "inward",
    "inwarder",
    "inwardest",
    "inwards",
    "is",
    "it",
    "its",
    "itself",
    "j",
    "k",
    "l",
    "latter",
    "latterer",
    "latterest",
    "latterly",
    "latters",
    "layabout",
    "layabouts",
    "less",
    "lest",
    "lot",
    "lots",
    "lotted",
    "lotting",
    "m",
    "main",
    "make",
    "many",
    "mauger",
    "maugre",
    "mayest",
    "me",
    "meanwhile",
    "meanwhiles",
    "midst",
    "midsts",
    "might",
    "mights",
    "more",
    "moreover",
    "most",
    "mostly",
    "much",
    "mucher",
    "muchest",
    "must",
    "musth",
    "musths",
    "musts",
    "my",
    "myself",
    "n",
    "natheless",
    "nathless",
    "neath",
    "neaths",
    "necessarier",
    "necessariest",
    "necessary",
    "neither",
    "nethe",
    "nethermost",
    "never",
    "nevertheless",
    "nigh",
    "nigher",
    "nighest",
    "nine",
    "no",
    "no-one",
    "nobodies",
    "nobody",
    "noes",
    "none",
    "noone",
    "nor",
    "nos",
    "not",
    "nothing",
    "nothings",
    "notwithstanding",
    "nowhere",
    "nowheres",
    "o",
    "of",
    "off",
    "offest",
    "offs",
    "often",
    "oftener",
    "oftenest",
    "oh",
    "on",
    "one",
    "oneself",
    "onest",
    "ons",
    "onto",
    "or",
    "orer",
    "orest",
    "other",
    "others",
    "otherwise",
    "otherwiser",
    "otherwisest",
    "ought",
    "oughts",
    "our",
    "ours",
    "ourself",
    "ourselves",
    "out",
    "outed",
    "outest",
    "outs",
    "outside",
    "outwith",
    "over",
    "overall",
    "overaller",
    "overallest",
    "overalls",
    "overs",
    "own",
    "owned",
    "owning",
    "owns",
    "owt",
    "p",
    "particular",
    "particularer",
    "particularest",
    "particularly",
    "particulars",
    "per",
    "perhaps",
    "plaintiff",
    "please",
    "pleased",
    "pleases",
    "plenties",
    "plenty",
    "pro",
    "probably",
    "provide",
    "provided",
    "provides",
    "providing",
    "q",
    "qua",
    "que",
    "quite",
    "r",
    "rath",
    "rathe",
    "rather",
    "rathest",
    "re",
    "really",
    "regarding",
    "relate",
    "related",
    "relatively",
    "res",
    "respecting",
    "respectively",
    "s",
    "said",
    "saider",
    "saidest",
    "same",
    "samer",
    "sames",
    "samest",
    "sans",
    "sanserif",
    "sanserifs",
    "sanses",
    "saved",
    "sayid",
    "sayyid",
    "seem",
    "seemed",
    "seeminger",
    "seemingest",
    "seemings",
    "seems",
    "send",
    "sent",
    "senza",
    "serious",
    "seriouser",
    "seriousest",
    "seven",
    "several",
    "severaler",
    "severalest",
    "shall",
    "shalled",
    "shalling",
    "shalls",
    "she",
    "should",
    "shoulded",
    "shoulding",
    "shoulds",
    "since",
    "sine",
    "sines",
    "sith",
    "six",
    "so",
    "sobeit",
    "soer",
    "soest",
    "some",
    "somebody",
    "somehow",
    "someone",
    "something",
    "sometime",
    "sometimer",
    "sometimes",
    "sometimest",
    "somewhat",
    "somewhere",
    "stop",
    "stopped",
    "such",
    "summat",
    "sup",
    "supped",
    "supping",
    "sups",
    "syn",
    "syne",
    "t",
    "ten",
    "than",
    "that",
    "the",
    "thee",
    "their",
    "theirs",
    "them",
    "themselves",
    "then",
    "thence",
    "thener",
    "thenest",
    "there",
    "thereafter",
    "thereby",
    "therefore",
    "therein",
    "therer",
    "therest",
    "thereupon",
    "these",
    "they",
    "thine",
    "thing",
    "things",
    "this",
    "thises",
    "thorough",
    "thorougher",
    "thoroughest",
    "thoroughly",
    "those",
    "thou",
    "though",
    "thous",
    "thouses",
    "three",
    "thro",
    "through",
    "througher",
    "throughest",
    "throughout",
    "thru",
    "thruer",
    "thruest",
    "thus",
    "thy",
    "thyself",
    "till",
    "tilled",
    "tilling",
    "tills",
    "to",
    "together",
    "too",
    "toward",
    "towarder",
    "towardest",
    "towards",
    "two",
    "u",
    "umpteen",
    "under",
    "underneath",
    "unless",
    "unlike",
    "unliker",
    "unlikest",
    "until",
    "unto",
    "up",
    "upon",
    "uponed",
    "uponing",
    "upons",
    "upped",
    "upping",
    "ups",
    "us",
    "use",
    "used",
    "usedest",
    "username",
    "usually",
    "v",
    "various",
    "variouser",
    "variousest",
    "verier",
    "veriest",
    "versus",
    "very",
    "via",
    "vis-a-vis",
    "vis-a-viser",
    "vis-a-visest",
    "viz",
    "vs",
    "w",
    "was",
    "wast",
    "we",
    "were",
    "wert",
    "what",
    "whatever",
    "whateverer",
    "whateverest",
    "whatsoever",
    "whatsoeverer",
    "whatsoeverest",
    "wheen",
    "when",
    "whenas",
    "whence",
    "whencesoever",
    "whenever",
    "whensoever",
    "where",
    "whereafter",
    "whereas",
    "whereby",
    "wherefrom",
    "wherein",
    "whereinto",
    "whereof",
    "whereon",
    "wheresoever",
    "whereto",
    "whereupon",
    "wherever",
    "wherewith",
    "wherewithal",
    "whether",
    "which",
    "whichever",
    "whichsoever",
    "while",
    "whiles",
    "whilst",
    "whither",
    "whithersoever",
    "whoever",
    "whomever",
    "whose",
    "whoso",
    "whosoever",
    "why",
    "with",
    "withal",
    "within",
    "without",
    "would",
    "woulded",
    "woulding",
    "woulds",
    "x",
    "y",
    "ye",
    "yet",
    "yon",
    "yond",
    "yonder",
    "you",
    "your",
    "yours",
    "yourself",
    "yourselves",
    "z",
    "zillion",
  };
  private static final String[] TERRIER_STEMMED_STOP_WORDS = {
"issu",
"contribut",
"descript",
"event",
"made",
"pleas",
"list",
"relationship",
"interrelationship",
"call",
"describ",
"link",
"definit",
"happen",
"characterist",
    "a",
    "abaft",
    "abaft",
    "abaftest",
    "about",
    "about",
    "aboutest",
    "abov",
    "abov",
    "abovest",
    "accord",
    "aer",
    "aest",
    "afor",
    "after",
    "after",
    "afterest",
    "afterward",
    "afterward",
    "again",
    "against",
    "aid",
    "ain",
    "albeit",
    "all",
    "aller",
    "allest",
    "all",
    "allyou",
    "almost",
    "along",
    "alongsid",
    "alreadi",
    "also",
    "although",
    "alway",
    "amid",
    "amidst",
    "among",
    "amongst",
    "an",
    "and",
    "andor",
    "anear",
    "anent",
    "anoth",
    "ani",
    "anybodi",
    "anyhow",
    "anyon",
    "anyth",
    "anywher",
    "apart",
    "apart",
    "apartest",
    "appear",
    "appear",
    "appear",
    "appear",
    "appropri",
    "appropri",
    "appropriat",
    "appropri",
    "appropriatest",
    "appropri",
    "are",
    "are",
    "around",
    "as",
    "ase",
    "asid",
    "asid",
    "aslant",
    "astraddl",
    "astraddl",
    "astraddlest",
    "astrid",
    "astrid",
    "astridest",
    "at",
    "athwart",
    "atop",
    "atween",
    "aught",
    "aught",
    "avail",
    "availabl",
    "availablest",
    "aw",
    "b",
    "be",
    "becam",
    "becaus",
    "becom",
    "becom",
    "becom",
    "becoming",
    "becomingest",
    "becom",
    "been",
    "befor",
    "beforehand",
    "beforehand",
    "beforehandest",
    "behind",
    "behind",
    "below",
    "beneath",
    "besid",
    "besid",
    "better",
    "better",
    "better",
    "better",
    "between",
    "betwixt",
    "beyond",
    "bist",
    "both",
    "but",
    "but",
    "by",
    "by-and-by",
    "byandbi",
    "c",
    "cannot",
    "canst",
    "cant",
    "cant",
    "cantest",
    "cant",
    "cant",
    "cer",
    "certain",
    "certain",
    "certainest",
    "cest",
    "chez",
    "circa",
    "co",
    "come-on",
    "come-on",
    "comeon",
    "comeon",
    "concern",
    "concerning",
    "concerningest",
    "consequ",
    "consid",
    "could",
    "couldst",
    "cum",
    "d",
    "dday",
    "dday",
    "describ",
    "describ",
    "describ",
    "describ",
    "despit",
    "despit",
    "despit",
    "despit",
    "did",
    "differ",
    "different",
    "differentest",
    "do",
    "doe",
    "doe",
    "do",
    "do",
    "done",
    "doner",
    "done",
    "donest",
    "dos",
    "dost",
    "doth",
    "down",
    "downward",
    "downward",
    "downwardest",
    "downward",
    "dure",
    "e",
    "each",
    "eg",
    "eight",
    "either",
    "els",
    "elsewher",
    "enough",
    "ere",
    "et",
    "etc",
    "even",
    "even",
    "evenest",
    "even",
    "evens",
    "evensest",
    "ever",
    "everi",
    "everybodi",
    "everyon",
    "everyth",
    "everywher",
    "ex",
    "except",
    "except",
    "except",
    "except",
    "exe",
    "f",
    "fact",
    "fact",
    "fail",
    "fail",
    "few",
    "fewer",
    "fewest",
    "figupon",
    "figupon",
    "figupon",
    "figupon",
    "five",
    "followthrough",
    "for",
    "forbi",
    "forby",
    "fore",
    "forer",
    "fore",
    "forev",
    "former",
    "former",
    "formerest",
    "former",
    "former",
    "fornenst",
    "forwhi",
    "four",
    "fourscor",
    "frae",
    "from",
    "fs",
    "further",
    "further",
    "further",
    "furtherest",
    "further",
    "furthermor",
    "further",
    "g",
    "get",
    "get",
    "get",
    "go",
    "gone",
    "good",
    "got",
    "gotta",
    "gotten",
    "h",
    "had",
    "hadst",
    "hae",
    "hard",
    "has",
    "hast",
    "hath",
    "have",
    "have",
    "have",
    "he",
    "henc",
    "her",
    "hereaft",
    "hereaft",
    "herebi",
    "herein",
    "hereupon",
    "her",
    "herself",
    "him",
    "himself",
    "his",
    "hither",
    "hither",
    "hitherest",
    "hoo",
    "hoo",
    "how",
    "how-do-you-do",
    "howbeit",
    "howdoyoudo",
    "howev",
    "huh",
    "humph",
    "i",
    "idem",
    "idem",
    "idemest",
    "ie",
    "if",
    "if",
    "immedi",
    "immedi",
    "immediat",
    "immediatest",
    "in",
    "inasmuch",
    "inc",
    "inde",
    "indic",
    "indic",
    "indic",
    "indic",
    "info",
    "inform",
    "insofar",
    "instead",
    "into",
    "inward",
    "inward",
    "inwardest",
    "inward",
    "is",
    "it",
    "it",
    "itself",
    "j",
    "k",
    "l",
    "latter",
    "latter",
    "latterest",
    "latter",
    "latter",
    "layabout",
    "layabout",
    "less",
    "lest",
    "lot",
    "lot",
    "lot",
    "lot",
    "m",
    "main",
    "make",
    "mani",
    "mauger",
    "maugr",
    "mayest",
    "me",
    "meanwhil",
    "meanwhil",
    "midst",
    "midst",
    "might",
    "might",
    "more",
    "moreov",
    "most",
    "most",
    "much",
    "mucher",
    "muchest",
    "must",
    "musth",
    "musth",
    "must",
    "my",
    "myself",
    "n",
    "natheless",
    "nathless",
    "neath",
    "neath",
    "necessari",
    "necessariest",
    "necessari",
    "neither",
    "neth",
    "nethermost",
    "never",
    "nevertheless",
    "nigh",
    "nigher",
    "nighest",
    "nine",
    "no",
    "no-one",
    "nobodi",
    "nobodi",
    "noe",
    "none",
    "noon",
    "nor",
    "nos",
    "not",
    "noth",
    "noth",
    "notwithstand",
    "nowher",
    "nowher",
    "o",
    "of",
    "off",
    "offest",
    "off",
    "often",
    "often",
    "oftenest",
    "oh",
    "on",
    "one",
    "oneself",
    "onest",
    "on",
    "onto",
    "or",
    "orer",
    "orest",
    "other",
    "other",
    "otherwis",
    "otherwis",
    "otherwisest",
    "ought",
    "ought",
    "our",
    "our",
    "ourself",
    "ourselv",
    "out",
    "out",
    "outest",
    "out",
    "outsid",
    "outwith",
    "over",
    "overal",
    "overal",
    "overallest",
    "overal",
    "over",
    "own",
    "own",
    "own",
    "own",
    "owt",
    "p",
    "particular",
    "particular",
    "particularest",
    "particular",
    "particular",
    "per",
    "perhap",
    "plaintiff",
    "pleas",
    "pleas",
    "pleas",
    "plenti",
    "plenti",
    "pro",
    "probabl",
    "provid",
    "provid",
    "provid",
    "provid",
    "q",
    "qua",
    "que",
    "quit",
    "r",
    "rath",
    "rath",
    "rather",
    "rathest",
    "re",
    "realli",
    "regard",
    "relat",
    "relat",
    "relat",
    "res",
    "respect",
    "respect",
    "s",
    "said",
    "saider",
    "saidest",
    "same",
    "samer",
    "same",
    "samest",
    "san",
    "sanserif",
    "sanserif",
    "sans",
    "save",
    "sayid",
    "sayyid",
    "seem",
    "seem",
    "seeming",
    "seemingest",
    "seem",
    "seem",
    "send",
    "sent",
    "senza",
    "serious",
    "serious",
    "seriousest",
    "seven",
    "sever",
    "several",
    "severalest",
    "shall",
    "shall",
    "shall",
    "shall",
    "she",
    "should",
    "should",
    "should",
    "should",
    "sinc",
    "sine",
    "sine",
    "sith",
    "six",
    "so",
    "sobeit",
    "soer",
    "soest",
    "some",
    "somebodi",
    "somehow",
    "someon",
    "someth",
    "sometim",
    "sometim",
    "sometim",
    "sometimest",
    "somewhat",
    "somewher",
    "stop",
    "stop",
    "such",
    "summat",
    "sup",
    "sup",
    "sup",
    "sup",
    "syn",
    "syne",
    "t",
    "ten",
    "than",
    "that",
    "the",
    "thee",
    "their",
    "their",
    "them",
    "themselv",
    "then",
    "thenc",
    "thener",
    "thenest",
    "there",
    "thereaft",
    "therebi",
    "therefor",
    "therein",
    "therer",
    "therest",
    "thereupon",
    "these",
    "they",
    "thine",
    "thing",
    "thing",
    "this",
    "thise",
    "thorough",
    "thorough",
    "thoroughest",
    "thorough",
    "those",
    "thou",
    "though",
    "thous",
    "thous",
    "three",
    "thro",
    "through",
    "througher",
    "throughest",
    "throughout",
    "thru",
    "thruer",
    "thruest",
    "thus",
    "thi",
    "thyself",
    "till",
    "till",
    "till",
    "till",
    "to",
    "togeth",
    "too",
    "toward",
    "toward",
    "towardest",
    "toward",
    "two",
    "u",
    "umpteen",
    "under",
    "underneath",
    "unless",
    "unlik",
    "unlik",
    "unlikest",
    "until",
    "unto",
    "up",
    "upon",
    "upon",
    "upon",
    "upon",
    "up",
    "up",
    "up",
    "us",
    "use",
    "use",
    "usedest",
    "usernam",
    "usual",
    "v",
    "various",
    "various",
    "variousest",
    "verier",
    "veriest",
    "versus",
    "veri",
    "via",
    "vis-a-vi",
    "vis-a-vis",
    "vis-a-visest",
    "viz",
    "vs",
    "w",
    "was",
    "wast",
    "we",
    "were",
    "wert",
    "what",
    "whatev",
    "whatever",
    "whateverest",
    "whatsoev",
    "whatsoever",
    "whatsoeverest",
    "wheen",
    "when",
    "whena",
    "whenc",
    "whencesoev",
    "whenev",
    "whensoev",
    "where",
    "whereaft",
    "wherea",
    "wherebi",
    "wherefrom",
    "wherein",
    "whereinto",
    "whereof",
    "whereon",
    "wheresoev",
    "whereto",
    "whereupon",
    "wherev",
    "wherewith",
    "wherewith",
    "whether",
    "which",
    "whichev",
    "whichsoev",
    "while",
    "while",
    "whilst",
    "whither",
    "whithersoev",
    "whoever",
    "whomev",
    "whose",
    "whoso",
    "whosoev",
    "whi",
    "with",
    "withal",
    "within",
    "without",
    "would",
    "would",
    "would",
    "would",
    "x",
    "y",
    "ye",
    "yet",
    "yon",
    "yond",
    "yonder",
    "you",
    "your",
    "your",
    "yourself",
    "yourselv",
    "z",
    "zillion"
  };
  private static final String[] FRENCH_SNOWBALL_STOP_WORDS = {
    "au",
    "aux",
    "avec",
    "ce",
    "ces",
    "dans",
    "de",
    "des",
    "du",
    "elle",
    "en",
    "et",
    "eux",
    "il",
    "je",
    "la",
    "le",
    "leur",
    "lui",
    "ma",
    "mais",
    "me",
    "même",
    "mes",
    "moi",
    "mon",
    "ne",
    "nos",
    "notre",
    "nous",
    "on",
    "ou",
    "par",
    "pas",
    "pour",
    "qu",
    "que",
    "qui",
    "sa",
    "se",
    "ses",
    "son",
    "sur",
    "ta",
    "te",
    "tes",
    "toi",
    "ton",
    "tu",
    "un",
    "une",
    "vos",
    "votre",
    "vous",
    "c",
    "d",
    "j",
    "l",
    "à",
    "m",
    "n",
    "s",
    "t",
    "y",
    "été",
    "étée",
    "étées",
    "étés",
    "étant",
    "suis",
    "es",
    "est",
    "sommes",
    "êtes",
    "sont",
    "serai",
    "seras",
    "sera",
    "serons",
    "serez",
    "seront",
    "serais",
    "serait",
    "serions",
    "seriez",
    "seraient",
    "étais",
    "était",
    "étions",
    "étiez",
    "étaient",
    "fus",
    "fut",
    "fûmes",
    "fûtes",
    "furent",
    "sois",
    "soit",
    "soyons",
    "soyez",
    "soient",
    "fusse",
    "fusses",
    "fût",
    "fussions",
    "fussiez",
    "fussent",
    "ayant",
    "eu",
    "eue",
    "eues",
    "eus",
    "ai",
    "as",
    "avons",
    "avez",
    "ont",
    "aurai",
    "auras",
    "aura",
    "aurons",
    "aurez",
    "auront",
    "aurais",
    "aurait",
    "aurions",
    "auriez",
    "auraient",
    "avais",
    "avait",
    "avions",
    "aviez",
    "avaient",
    "eut",
    "eûmes",
    "eûtes",
    "eurent",
    "aie",
    "aies",
    "ait",
    "ayons",
    "ayez",
    "aient",
    "eusse",
    "eusses",
    "eût",
    "eussions",
    "eussiez",
    "eussent",
    "ceci",
    "celà",
    "cet",
    "cette",
    "ici",
    "ils",
    "les",
    "leurs",
    "quel",
    "quels",
    "quelle",
    "quelles",
    "sans",
    "soi",
    "l'",
  "d'"};
  private static final String[] FRENCH_SNOWBALL_STEMMED_STOP_WORDS = {
    "au",
    "aux",
    "avec",
    "ce",
    "ce",
    "dan",
    "de",
    "de",
    "du",
    "elle",
    "en",
    "et",
    "eux",
    "il",
    "je",
    "la",
    "le",
    "leur",
    "lui",
    "ma",
    "mais",
    "me",
    "mêm",
    "me",
    "moi",
    "mon",
    "ne",
    "nos",
    "notr",
    "nous",
    "on",
    "ou",
    "par",
    "pas",
    "pour",
    "qu",
    "que",
    "qui",
    "sa",
    "se",
    "se",
    "son",
    "sur",
    "ta",
    "te",
    "te",
    "toi",
    "ton",
    "tu",
    "un",
    "une",
    "vos",
    "votr",
    "vous",
    "c",
    "d",
    "j",
    "l",
    "à",
    "m",
    "n",
    "s",
    "t",
    "y",
    "été",
    "été",
    "été",
    "été",
    "étant",
    "suis",
    "e",
    "est",
    "somm",
    "ête",
    "sont",
    "ser",
    "ser",
    "ser",
    "seron",
    "ser",
    "seront",
    "ser",
    "ser",
    "serion",
    "ser",
    "ser",
    "étais",
    "était",
    "étion",
    "éti",
    "étaient",
    "fus",
    "fut",
    "fûm",
    "fût",
    "furent",
    "sois",
    "soit",
    "soyon",
    "soi",
    "soient",
    "fuss",
    "fuss",
    "fût",
    "fussion",
    "fuss",
    "fussent",
    "ayant",
    "eu",
    "eue",
    "eue",
    "eus",
    "ai",
    "as",
    "avon",
    "avez",
    "ont",
    "aur",
    "aur",
    "aur",
    "auron",
    "aur",
    "auront",
    "aur",
    "aur",
    "aurion",
    "aur",
    "aur",
    "avais",
    "avait",
    "avion",
    "avi",
    "avaient",
    "eut",
    "eûm",
    "eût",
    "eurent",
    "aie",
    "aie",
    "ait",
    "ayon",
    "ayez",
    "aient",
    "euss",
    "euss",
    "eût",
    "eussion",
    "euss",
    "eussent",
    "cec",
    "celà",
    "cet",
    "cet",
    "ici",
    "il",
    "le",
    "leur",
    "quel",
    "quel",
    "quel",
    "quel",
    "san",
    "soi",
    "l'",
    "d'"
  };
  private final Set<String> englishStopwords = Sets.newHashSet(TERRIER_STOP_WORDS);
  private final Set<String> englishStemmedStopwords = Sets.newHashSet(TERRIER_STEMMED_STOP_WORDS);
  private final Set<String> frenchStopwords = Sets.newHashSet(FRENCH_SNOWBALL_STOP_WORDS);
  private final Set<String> frenchStemmedStopwords = Sets.newHashSet(FRENCH_SNOWBALL_STEMMED_STOP_WORDS);

  public OpenNLPTokenizer(){
    super();
  }

  @Override
  public void configure(Configuration conf){
    FileSystem fs;
    try {
      fs = FileSystem.get(conf);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } 
    configure(conf, fs);
  }

  @Override
  public void configure(Configuration mJobConf, FileSystem fs){
    setTokenizer(fs, new Path(mJobConf.get(Constants.TokenizerData)));
    if (mJobConf.getBoolean(Constants.Stemming, true)) {
      setLanguageAndStemmer(mJobConf.get(Constants.Language));
    }else {
      setLanguage(mJobConf.get(Constants.Language));
    }
    VocabularyWritable vocab;
    try {
      vocab = (VocabularyWritable) HadoopAlign.loadVocab(new Path(mJobConf.get(Constants.CollectionVocab)), fs);
      setVocab(vocab);
    } catch (Exception e) {
      sLogger.warn("No vocabulary provided to tokenizer.");
      vocab = null;
    }
    isStopwordRemoval = mJobConf.getBoolean(Constants.Stopword, true);
    sLogger.warn("Stopword removal is " + isStopwordRemoval);
  }

  public void setTokenizer(FileSystem fs, Path p){
    try {
      FSDataInputStream in = fs.open(p);
      TokenizerModel model;
      model = new TokenizerModel(in);
      tokenizer = new TokenizerME(model);
    }
    catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void setLanguage(String l){
    if(l.startsWith("en")){
      lang = ENGLISH;//"english";
    }else if(l.startsWith("fr")){
      lang = FRENCH;//"french";
    }else if(l.equals("german") || l.startsWith("de")){
      lang = GERMAN;//"german";
    }else{
      sLogger.warn("Language not recognized, setting to English!");
    }
  }

  @SuppressWarnings("unchecked")
  public void setLanguageAndStemmer(String l){
    if(l.startsWith("en")){
      lang = ENGLISH;//"english";
    }else if(l.startsWith("fr")){
      lang = FRENCH;//"french";
    }else if(l.equals("german") || l.startsWith("de")){
      lang = GERMAN;//"german";
    }else{
      sLogger.warn("Language not recognized, setting to English!");
    }
    Class stemClass;
    try {
      stemClass = Class.forName("org.tartarus.snowball.ext." +
          languages[lang] + "Stemmer");
      stemmer = (SnowballStemmer) stemClass.newInstance();
    } catch (ClassNotFoundException e) {
      sLogger.warn("Stemmer class not recognized!\n"+"org.tartarus.snowball.ext." +
          languages[lang] + "Stemmer");
      stemmer = null;
      return;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } 
  }

  @Override
  public String[] processContent(String text) {
    text = preNormalize(text);
    if ( lang == FRENCH ) {
      text = text.replaceAll("'", "' ");    // openNLP does not separate what comes after the apostrophe, which seems to work better
    }

    String[] tokens = tokenizer.tokenize(text);
    String tokenizedText = "";
    for ( String token : tokens ){
      tokenizedText += token + " ";
    }

    // do post-normalizations before any stemming or stopword removal 
    String[] normalizedTokens = postNormalize(tokenizedText).split(" ");
    tokenizedText = "";
    for ( int i = 0; i < normalizedTokens.length; i++ ){
      String token = normalizedTokens[i].toLowerCase();
      if ( isStopwordRemoval && isDiscard(token) ) {
        //        sLogger.warn("Discarded stopword "+token);
        continue;
      }

      //apply stemming on token
      String stemmedToken = token;
      if ( stemmer!=null ) {
        stemmer.setCurrent(token);
        stemmer.stem();
        stemmedToken = stemmer.getCurrent();
      }

      //skip if out of vocab
      if ( vocab != null && vocab.get(stemmedToken) <= 0) {
        //        sLogger.warn("Discarded OOV "+token);
        continue;
      }
      tokenizedText += (stemmedToken + " ");
    }

    return tokenizedText.trim().split(" ");
  }

  public String getLanguage() {
    return languages[lang];
  }

  @Override
  public int getNumberTokens(String string){
    return tokenizer.tokenize(string).length;
  }

  private boolean isDiscard(String token) {
    // remove characters that may cause problems when processing further
    //    token = removeNonUnicodeChars(token);

    return ( token.length() < MIN_LENGTH || token.length() > MAX_LENGTH || delims.contains(token) || (lang==ENGLISH && englishStopwords.contains(token)) || (lang==FRENCH && frenchStopwords.contains(token)) );
  }
  
  @Override
  public String stem(String token) {
    token = postNormalize(preNormalize(token)).toLowerCase();
    if ( stemmer!=null ) {
      stemmer.setCurrent(token);
      stemmer.stem();
      return stemmer.getCurrent();
    }else {
      return token;
    }
  }

  /* 
   * For external use. returns true if token is a Galago stopword or a delimiter: `~!@#$%^&*()-_=+]}[{\\|'\";:/?.>,<
   */
  @Override
  public boolean isStopWord(String token) {
    return (lang==ENGLISH && (englishStopwords.contains(token) || delims.contains(token))) || (lang==FRENCH && (frenchStopwords.contains(token) || delims.contains(token)));
  }
  
  @Override
  public boolean isStemmedStopWord(String token) {
    return (lang==ENGLISH && (englishStemmedStopwords.contains(token) || delims.contains(token))) || (lang==FRENCH && (frenchStemmedStopwords.contains(token) || delims.contains(token)));
  }

}