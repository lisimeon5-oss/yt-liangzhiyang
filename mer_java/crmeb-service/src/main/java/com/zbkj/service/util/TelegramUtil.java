package com.zbkj.service.util;


import cn.hutool.json.JSONObject;
import com.zbkj.common.utils.HttpUtil;
import com.zbkj.common.utils.ThreadPool;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0.0
 * @ClassName TelegramNoticeUtil
 * @Description 小飞机通知工具类
 * @Date Dec 28, 2019 9:28:33 PM
 */
@Component
public class TelegramUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final Logger log = LoggerFactory.getLogger(TelegramUtil.class);

    /**
     * 访问地址
     */
    private final static String api_url = "https://api.telegram.org/";

    //旧
    // private final static String token = "bot7484611566:AAH40E2ExgZEbxvf6qkNyUh4W7dG1U5l5vk";

    //新
    private final static String token = "bot8024229995:AAH1mTTBRFIDFT0Xf01VstSWsqdOks76Byw";

    //送单
    private final static String DELIVERY_GROUP_ID = "-4986921349";
    //private final static String DELIVERY_GROUP_ID = "tmp";
    //新订单
    private final static String chastId = "-4956679349";
    //private final static String chastId = "tmp";

    private final static String bot_url = "https://announce.bigbet.world/Notice/SendNotice";


    /**
     * 小飞机发送消息 string : message
     *
     * @param message
     */
    public static void sendMessage(String message) {
        try {
            if (StringUtils.isBlank(message)) {
                return;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    sendTelegramNoticeMessage(chastId, message);
                }
            };
            ThreadPool.submit(runnable);
        } catch (Exception e) {
            log.error("发送小飞机支付通知异常:{}", e);
        }
    }

    /**
     * 小飞机发送消息 string : message
     *
     * @param message
     */
    public static void sendMessage(String chatId, String message) {
        try {
            if (StringUtils.isBlank(message)) {
                return;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    sendTelegramNoticeMessage(chatId, message);
                }
            };
            ThreadPool.submit(runnable);
        } catch (Exception e) {
            log.error("发送小飞机支付通知异常:{}", e);
        }
    }

    private static void sendTelegramNoticeMessage(String chastId, String message) {
        try {
            String url = api_url + token + "/sendMessage";
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("chat_id", chastId);
            data.put("text", message);
            HttpUtil.toPostJsonObj(url, data);
        } catch (Exception e) {
            log.info("发送小飞机消息异常: ", e);
        }
    }

    /**
     * 小飞机发送消息 string : message
     *
     * @param message
     */
    public void sendMessageDelivery(String orderNo,String message, Long customerChatId, String merchantName, String tgKf, String deliveryPhone, String groupId) {
        try {
            if (StringUtils.isBlank(message)) {
                return;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    sendTelegramNoticeMessageDelivery(orderNo,message, customerChatId, merchantName, tgKf, deliveryPhone, groupId);
                }
            };
            ThreadPool.submit(runnable);
        } catch (Exception e) {
            log.error("发送小飞机支付通知异常:{}", e);
        }
    }

    //配送通知
    private void sendTelegramNoticeMessageDelivery(String orderNo,String message, Long customerChatId, String merchantName, String tgKf, String deliveryPhone, String groupId) {
        try {
            String url = api_url + token + "/sendMessage";
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("chat_id", groupId);
            data.put("text", message);
            data.put("parse_mode", "HTML");
            // 创建按钮
            Map<String, Object> replyMarkup = new HashMap<>();
            List<List<Map<String, String>>> keyboard = new ArrayList<>();
            // 添加一个按钮
            List<Map<String, String>> row = new ArrayList<>();
            Map<String, String> button = new HashMap<>();
            button.put("text", "ငါရောက်ပြီ၊ လာလာယူပါ။(我已到达,请客人来取)");

            JSONObject callbackData = new JSONObject();
            callbackData.put("type", 5);
            callbackData.put("command", "/arrived");
            //callbackData.put("data", customerChatId + "," + deliveryPhone + "," + merchantName + "," + tgKf);
            //callbackData.put("data", customerChatId + "," + deliveryPhone  );
            // 1. 生成短 ID（只取前 8 位）
            String uuid = UUID.randomUUID().toString().substring(0, 8);
            // 2. 完整数据拼接
            String fullData = String.join(",",
                    String.valueOf(customerChatId),
                    deliveryPhone,
                    merchantName,
                    tgKf,
                    orderNo
                    );
            // 3. 存 Redis（10个小时过期）
            stringRedisTemplate.opsForValue().set("cb:" + uuid, fullData, 600, TimeUnit.MINUTES);

           // log.info("========== Redis 存成功 ==========");
            // 验证是否存成功
            //String check =  stringRedisTemplate.opsForValue().get("cb:" + uuid);
            // log.info("验证读取: {}", check);
            // 4. callback_data 只存 ID
            callbackData.put("data", uuid);
            button.put("callback_data", callbackData.toString()); // 回调数据
            row.add(button);

            List<Map<String, String>> successRow = new ArrayList<>();
            Map<String, String> successButton = new HashMap<>();
            successButton.put("text", "Delivery ပြီးပါပြီ။（配送完成）");
            JSONObject successCallbackData = new JSONObject();
            successCallbackData.put("type", 5);
            successCallbackData.put("command", "/deliverySuccess");
            successButton.put("callback_data", successCallbackData.toString()); // 回调数据
            successRow.add(successButton);

            keyboard.add(row);
            keyboard.add(successRow);
            replyMarkup.put("inline_keyboard", keyboard);
            data.put("reply_markup", replyMarkup);

            HttpUtil.toPostJsonObj(url, data);
        } catch (Exception e) {
            log.info("发送小飞机消息异常: ", e);
        }
    }


    public static void main(String[] args) throws Exception {
        String[] numbers = {
                "7736811706",
                "7889840539",
                "2070479263",
                "6419687146",
                "7557769043",
                "6284431427",
                "881839798",
                "7771358627",
                "6127710566",
                "7522752634",
                "6942479748",
                "6667820327",
                "7620588940",
                "5337291009",
                "6222354500",
                "7941512543",
                "7627932134",
                "5522803559",
                "8143137774",
                "7432369454",
                "5983716461",
                "6991605586",
                "7669613114",
                "5850861370",
                "7594015456",
                "6608116713",
                "6096017628",
                "6967233181",
                "7897515692",
                "7693116385",
                "7232179059",
                "6129055085",
                "7689517021",
                "5080269059",
                "8177606080",
                "7907533658",
                "6630133538",
                "6902760821",
                "1896465541",
                "7642200729",
                "7142250632",
                "7186352699",
                "6583231609",
                "5195427662",
                "5683524074",
                "5810583379",
                "7376724475",
                "7923267914",
                "8099323990",
                "7260800061",
                "6885867918",
                "8097104259",
                "7686231503",
                "7476067220",
                "7504293110",
                "7418867023",
                "6425781756",
                "6453506802",
                "2054384369",
                "8000699030",
                "7285852986",
                "5621832274",
                "5567097821",
                "7223223299",
                "1880062292",
                "6023417292",
                "7422883300",
                "6595155404",
                "8145644339",
                "7741782376",
                "5346518404",
                "7407379788",
                "6814638958",
                "6451978326",
                "7563141747",
                "7824430932",
                "7608671635",
                "5901212434",
                "7900799083",
                "5951815370",
                "6384027336",
                "7986817672",
                "6741816645",
                "1942673751",
                "7932281661",
                "7648039691",
                "6115335266",
                "5988026626",
                "8195229049",
                "7365197416",
                "7545408291",
                "6095483869",
                "8184531853",
                "7249098256",
                "5508714638",
                "8037919347",
                "1634508776",
                "6628514261",
                "5944601345",
                "7203308442",
                "7173075819",
                "6979772853",
                "6150634881",
                "6180188156",
                "5441765935",
                "6518517258",
                "7969655759",
                "5879293378",
                "7111786976",
                "5136195235",
                "7638522086",
                "7107663086",
                "7460316017",
                "6239351038",
                "5171361433",
                "7991361382",
                "5978860377",
                "5647249101",
                "1944323746",
                "6910634918",
                "6708600714",
                "5861228520",
                "7718993998",
                "6797094783",
                "7692344181",
                "6615020426",
                "5111288913",
                "1697285060",
                "7951190261",
                "7562723846",
                "7742931436",
                "7732724482",
                "7217984164",
                "7511941268",
                "7593223511",
                "7339894223",
                "7738830266",
                "6526487071",
                "7759736523",
                "6115316488",
                "6759533517",
                "7943237700",
                "6892231151",
                "7675090979",
                "5262089890",
                "6137514878",
                "7666193100",
                "7660593313",
                "5759565245",
                "5368456502",
                "7613582728",
                "6172258106",
                "7564870508",
                "7679109894",
                "7911700305",
                "7691816858",
                "7215410911",
                "5375971690",
                "6421158676",
                "6998067144",
                "6260584614",
                "7816331766",
                "6642553467",
                "7672766081",
                "6065683469",
                "6150526540",
                "7351436192",
                "7240308936",
                "7739963660",
                "8044945589",
                "8169661516",
                "6051954437",
                "6381599715",
                "6549804963",
                "6985333377",
                "5907338730",
                "7897196306",
                "6518514821",
                "8066894684",
                "6943130439",
                "7620749386",
                "5907605491",
                "8069639560",
                "6351086873",
                "6202499549",
                "5481191087",
                "7590401821",
                "1691907175",
                "8013794289",
                "7119670983",
                "6903755417",
                "7439945087",
                "7557702591",
                "7506488017",
                "5658072204",
                "7042313604",
                "7302841477",
                "7920054629",
                "7401893476",
                "7322319233",
                "7288136724",
                "2040942180",
                "2131119292",
                "5538228966",
                "7833686523",
                "7665487738",
                "7212439637",
                "1860894684",
                "8335084147",
                "7449472826",
                "5060671074",
                "6687321286",
                "5043250805",
                "1160029005",
                "6649466287",
                "6992383346",
                "7496747384",
                "569611105",
                "7468815017",
                "7911364328",
                "8014456271",
                "7215284248",
                "7189852570",
                "7550513204",
                "7395257122",
                "7400716002",
                "7168232820",
                "7457182763",
                "7207999371",
                "7712680328",
                "5618833456",
                "5398364145",
                "5728781173",
                "6705831612",
                "6952404967",
                "7397257856",
                "7344804431",
                "5018692124",
                "7284573655",
                "5934526081",
                "6286303421",
                "7826209372",
                "7449988767",
                "5275450085",
                "6727412007",
                "7248757040",
                "7453325460",
                "7803631323",
                "906235545",
                "1147653957",
                "7069225316",
                "7568737181",
                "5283016367",
                "8151684310",
                "7799935893",
                "8168801285",
                "7931550770",
                "7083693014",
                "6800778158",
                "5885048076",
                "5900573819",
                "6480638347",
                "7349015511",
                "7533856455",
                "7733230714",
                "6958955244",
                "7215337539",
                "7278979621",
                "7259075863",
                "7313874281",
                "7571040564",
                "7248627532",
                "7387949594",
                "7451333480",
                "8081157432",
                "7126737686",
                "7501175304",
                "1531770573",
                "7376102147",
                "6283198056",
                "6435306605",
                "5013885157",
                "6111199708",
                "7170047097",
                "6163211143",
                "6468917201",
                "1179859862",
                "8086020797",
                "8338638386",
                "6776527766",
                "8376729402",
                "7593358237",
                "1559046316",
                "7001480935",
                "6977065683",
                "8018339279",
                "6492649950",
                "7442322616",
                "7745129410",
                "7869303856",
                "6429053949",
                "7539377921",
                "7289685621",
                "6763282803",
                "7382206992",
                "7622791048",
                "7273027921",
                "5449792036",
                "7392738054",
                "7475470309",
                "5394809311",
                "7918516825",
                "7926473315",
                "8054356808",
                "5363633876",
                "6965859213",
                "2026800513",
                "7693738883",
                "7995897908",
                "5891182764",
                "2126592606",
                "5494104924",
                "6801266434",
                "7949152642",
                "6926786392",
                "6674440453",
                "6440396014",
                "7944406840",
                "7815629825",
                "7640908257",
                "7213407736",
                "7594589079",
                "7587267719",
                "7753101737",
                "6800522418",
                "7786676625",
                "1928758504",
                "7765253683",
                "7271128758",
                "7497510458",
                "8018960234",
                "6886700019",
                "6720457150",
                "5260846422",
                "7981316809",
                "7420954932",
                "7206239430",
                "6590643876",
                "8087855761",
                "7487030412",
                "5947813876",
                "5353885426",
                "5533601968",
                "7696021620",
                "5266276670",
                "7770653576",
                "7113470021",
                "7073546625",
                "5863633914",
                "7598275205",
                "1718381854",
                "5223135883",
                "7823135485",
                "2024014525",
                "7413330912",
                "7215587449",
                "6447551974",
                "6362897110",
                "7737506615",
                "7004725447",
                "6835607304",
                "7555816235",
                "6247270844",
                "6611038295",
                "7696843855",
                "7964593601",
                "6226881573",
                "6859261920",
                "7328264207",
                "7278749548",
                "8102927398",
                "6738786807",
                "5283637449",
                "7733700837",
                "5364467209",
                "8350298172",
                "6781961555",
                "8132043104",
                "8301432450",
                "6483834600",
                "8049814144",
                "8011798037",
                "7090319091",
                "6331750348",
                "6672690592",
                "6829833163",
                "5748150476",
                "1114490283",
                "6949980275",
                "5410459658",
                "6640224729",
                "7853938349",
                "6930737676",
                "5785283372",
                "7708779300",
                "7903215203",
                "7659337101",
                "5814377887",
                "7159211524",
                "7001835157",
                "8096403722",
                "7958528235",
                "7085033956",
                "5415843476",
                "6692098632",
                "7449479500",
                "7429097451",
                "7257296996",
                "6348147808",
                "8431399583",
                "7934241399",
                "8096983991",
                "6154295309",
                "7703827623",
                "6882856962",
                "1090572509",
                "7338235616",
                "7843697116",
                "1144503551",
                "7833935040",
                "7826346720",
                "7947899435",
                "6751040004",
                "7164716031",
                "7543748635",
                "6489840969",
                "6406564438",
                "8018829130",
                "7966879659",
                "6287227473",
                "7569703291",
                "6664469648",
                "6014394075",
                "7405105287",
                "8011520143",
                "6517623106",
                "5512852018",
                "7505223451",
                "7850902244",
                "6461480108",
                "6003464609",
                "6610016477",
                "6701604004",
                "6741652722",
                "6597877070",
                "7965584695",
                "7243371058",
                "7085288013",
                "7882207260",
                "7295554487",
                "6904871832",
                "6522525759",
                "8023344268",
                "7108850317",
                "7119568283",
                "7418416588",
                "7185909367",
                "1136633216",
                "6891978532",
                "6315732120",
                "6228240367",
                "6444853372",
                "8163597450",
                "2071363653",
                "6134563126",
                "7089597870",
                "7311198001",
                "7719401822",
                "6102358599",
                "7482757988",
                "1449280199",
                "7127616116",
                "5287695953",
                "5407961966",
                "1653002674",
                "6254641638",
                "6176325893",
                "6692976184",
                "1885891038",
                "6026662959",
                "6615693810",
                "8037431713",
                "5385973447",
                "7760704403",
                "6681712943",
                "6737886523",
                "6783893477",
                "7178030253",
                "7433295316",
                "6774705150",
                "7136592131",
                "6827821180",
                "6609079390",
                "5450157884",
                "8064860151",
                "7539994985",
                "7806866190",
                "7836025459",
                "5912866291",
                "5244295783",
                "7996405214",
                "7080328547",
                "7250398484",
                "7009083709",
                "5046862760",
                "7589265477",
                "8482994144",
                "6483418357",
                "8110983033",
                "7246933548",
                "7535071631",
                "5176561394",
                "6949872038",
                "7421200855",
                "7508240541",
                "7253391273",
                "7941048414",
                "7569925947",
                "7331513901",
                "7615376685",
                "8145909993",
                "7528593281",
                "7131375061",
                "6002657029",
                "7466162214",
                "6882334096",
                "5317363034",
                "7549730461",
                "7953007667",
                "7460797666",
                "7060501031",
                "7757541957",
                "6136890206",
                "6927472181",
                "5870820424",
                "8273034267",
                "6172597399",
                "6537987981",
                "6744009887",
                "8095908447",
                "6489840779",
                "7116866744",
                "7577065751",
                "8428711775",
                "6459240521",
                "5770570911",
                "5919729092",
                "6749908522",
                "8102119856",
                "5271455469",
                "8437889851",
                "7499352554",
                "5136561068",
                "8403206524",
                "7034788088",
                "8007452272",
                "5216206627",
                "1418387053",
                "7412221403",
                "7844385103",
                "5868617109",
                "6466752846",
                "6286750925",
                "2116849522",
                "6566104156",
                "7942433208",
                "5115681138",
                "7824503306",
                "8184076059",
                "7631984630",
                "7415199778",
                "7313509443",
                "7081936615",
                "6818149214",
                "7204519998",
                "6799376102",
                "7741869601",
                "7951532023",
                "8160092058",
                "515530069",
                "7511831522",
                "5019747892",
                "6308094514",
                "5640527664",
                "6571233324",
                "8475991294",
                "6525328358",
                "1695745483",
                "6825875454",
                "6543804246",
                "5836904575",
                "8499422447",
                "6868352027",
                "7137914502",
                "7181829981",
                "5855110535",
                "6266412926",
                "6258996400",
                "7493614147",
                "7779660181",
                "7339575544",
                "8188529002",
                "7391160802",
                "6119237582",
                "7699535434",
                "7789714164",
                "6378770914",
                "8123374995",
                "1892339184",
                "7867237897",
                "5892336142",
                "5604237258",
                "5457823490",
                "308359359",
                "7690055744",
                "5798754378",
                "7855925313",
                "5466695167",
                "5877250003",
                "6026198818",
                "6781029225",
                "7908698270",
                "7497389709",
                "8118587180",
                "5898047289",
                "8276697445",
                "8460806934",
                "7103062410",
                "8168788970",
                "7848579387",
                "8490760764",
                "5646920697",
                "6150252662",
                "7131414047",
                "6258432523",
                "8478768788",
                "7479655785",
                "7226073944",
                "7125112240",
                "7526447648",
                "5886729389",
                "5431039668",
                "7595666027",
                "7452760626",
                "5718309352",
                "7926592423",
                "8128461710",
                "5666335954",
                "6976555879",
                "6792710493",
                "7473913591",
                "5216644921",
                "1980464421",
                "8149141178",
                "7892235431",
                "5681255911",
                "5981808185",
                "6389377996",
                "7266638884",
                "6844792152",
                "1941922674",
                "5241262192",
                "6863762372",
                "7171132387",
                "7379426917",
                "1729625499",
                "7651009431",
                "1815940632",
                "6378973236",
                "7760074060",
                "6485247631",
                "6227444115",
                "6932860453",
                "1720073728",
                "6320371048",
                "6866513650",
                "5675087944",
                "6090350886",
                "5371784368",
                "7960453718",
                "5747217948",
                "5055827423",
                "7568093108",
                "7648991277",
                "6190682199",
                "7253573900",
                "6527522011",
                "7604028853",
                "8491366036",
                "1231157386",
                "6580700533",
                "7770472856",
                "7165679596",
                "5267236876",
                "8418149179",
                "6108904026",
                "7477988496",
                "7895306718",
                "6896941727",
                "5702897356",
                "6366936022",
                "6585728249",
                "6579426613",
                "7498514182",
                "6616453516",
                "6533832792",
                "6335581134",
                "7012106326",
                "6389166863",
                "7870202483",
                "1782839444",
                "5463582716",
                "5999295568",
                "7262058142",
                "7937621649",
                "8259189726",
                "8256964397",
                "8036656331",
                "7913649845",
                "630195634",
                "7401454793",
                "8011303925",
                "973260639",
                "6776739572",
                "1759985996",
                "6683409497",
                "6004425196",
                "1109113681",
                "6233815024",
                "6929679479",
                "8418375440",
                "7643129499",
                "5545911873",
                "5676054866",
                "5631032681",
                "7838379459",
                "5913441994",
                "6565940674",
                "7928309797",
                "7304062563",
                "7699337948",
        };

        String[] numberstmp = {
                "7736811706",
                "6406564438"

                //"8491366036"
        };
        List<String> stringList = Arrays.asList(numberstmp);
        for (String str : stringList) {
            /*sendMessage(str, "\uD83D\uDD25【秒杀时间调整通知】\uD83D\uDD25\n" +
                    "\n" +
                    "\uD83D\uDCE3 宝子们注意啦！明天秒杀继续狂欢！\uD83C\uDF89\n" +
                    "\n" +
                    "⏰ 时间调整说明：\n" +
                    "由于早场&午场间隔太短，订单爆仓\uD83D\uDE97\uD83D\uDCA8\n" +
                    "小弟们配送不过来，为确保大家准时收货，决定：\n" +
                    "✅ 早场提前1小时：\n" +
                    "【泰国10:00 | 北京11:00】\n" +
                    "✅ 午场时间不变：\n" +
                    "【泰国13:00 | 北京14:00】\n" +
                    "\n" +
                    "\uD83C\uDF1E 早场新增爆品（泰国10:00）：\n" +
                    "1\uFE0F⃣ 豌豆5包 — 1฿\n" +
                    "2\uFE0F⃣ 大衣架(3个装) — 1฿\n" +
                    "3\uFE0F⃣ 拉布布盲盒 — 59฿ （原价250฿）\n" +
                    "4\uFE0F⃣ 巴旦木(250g) — 19.9฿ \uD83D\uDCA5（市价150+）\n" +
                    "5\uFE0F⃣ 腰果大桶(500g+) — 99฿（市价500+）\n" +
                    "\n" +
                    "\uD83C\uDF19 午场新增爆品（泰国13:00）：\n" +
                    "1\uFE0F⃣ 劲仔小鱼5包 — 1฿\n" +
                    "2\uFE0F⃣ 荷花香烟 — 99฿ \uD83D\uDEAC\n" +
                    "⚠\uFE0F (国内直供真烟) 限量1盒\n" +
                    "\n" +
                    "\uD83D\uDCDC 重要规则：\n" +
                    "✨ 今日已秒商品明日不可重复秒\n" +
                    "✨ 可秒今日未抢到的商品\n" +
                    "✨ 重点关注新上架爆品\n" +
                    "\n" +
                    "\uD83D\uDE9A 配送保障：\n" +
                    "秒到即发！全力配送到底！\n" +
                    "\n" +
                    "\uD83D\uDC9D 感谢宝子们理解~\n" +
                    "明天继续拼手速！定好闹钟⏰\n" +
                    "\n" +
                    "#秒杀升级 #时间调整 #新增好货");*/
            // sendTelegramNoticePhoto(str,"thies");
            sendMessage(str, "您好：\n" +
                    "  亲爱的两只羊商城用户，小店在正常营业的，\n" +
                    "  但今天因为跑腿人员不足的问题，您的订单暂时配送不了噢！！\n" +
                    "  向您表达歉意！\n" +
                    "  您的飞机限制了信息发送，我们客服无法发信息联系您！\n" +
                    "  您 有问题可以联系我们客服号： @lzybh66.\n" +
                    "  网络不好的话，可以打店内号码：+95 9693384136\n" +
                    "  再次向您表达歉意！\n");
        }
    }

    /**
     * 小飞机发送消息 string : message
     *
     * @param message
     */
    public static void sendPhoto(String message) {
        try {
            if (StringUtils.isBlank(message)) {
                return;
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    sendTelegramNoticePhoto(chastId, message);
                }
            };
            ThreadPool.submit(runnable);
        } catch (Exception e) {
            log.error("发送小飞机支付通知异常:{}", e);
        }
    }

    private static void sendTelegramNoticePhoto(String chastId, String message) {
        try {
            String url = api_url + token + "/sendPhoto";
            Map<String, String> data = new HashMap<String, String>();
            data.put("chat_id", chastId);
            data.put("photo", "C:\\Users\\Administrator\\Desktop\\图片\\3c数码\\品牌\\联想\\2575653_b_1252984426930.jpg");
            //data.put("photo", "https://www.google.com/search?sca_esv=ed31745f76a805c8&sxsrf=AE3TifP9fdEGJTACOhDLXb1rxTgMdfzRWQ:1752761933211&q=%E6%96%B0&udm=2&fbs=AIIjpHybaGNnaZw_4TckIDK59Rtx7EmYoHRazOl26McMSIhEN76r--b_JoN4qNq4Vf6qU61-8fvqUN8XvRgAd9VzRBW95DxMnICuwYJ8l6_a7SUJbGCaQ3deuaYxWPwoQJDP79bIpvLZOF76kF9A09p2m2h8vxa7_vhukBfBpsYRlqeNXcfLTamvFYhRBx0r-vejpY1sjkgG0Qf3zYyge3I7x496xa6MxA&sa=X&ved=2ahUKEwifq56Di8SOAxVK4zgGHfRaMfgQtKgLKAF6BAgQEAE&biw=1536&bih=738&dpr=1.25#vhid=ehwCkJDPzLMl8M&vssid=mosaic");
            data.put("caption", message);
            HttpUtil.toPostForm(url, data);
        } catch (Exception e) {
            log.info("发送小飞机消息异常: ", e);
        }
    }


    /**
     * 处理需要屏蔽玩家姓名
     *
     * @param userName
     * @param shieldNumber
     * @return
     */
    private static String editUserName(String userName, Integer shieldNumber) {
        StringBuffer sb = new StringBuffer();
        if (0 < shieldNumber && userName.length() > shieldNumber) {
            int length = userName.length() - shieldNumber;
            for (int i = 0; i < length; i++) {
                sb.append("\\*");
            }
            userName = sb.append(userName.substring(length)).toString();
        } else if (userName.length() <= shieldNumber) {
            for (int i = 0; i < userName.length(); i++) {
                sb.append("\\*");
            }
            userName = sb.toString();
        }
        return userName;
    }


    private static String getName(String userName, Integer shieldNumber) {
        StringBuffer sb = new StringBuffer();
        if (0 < shieldNumber && userName.length() > shieldNumber) {
            int length = userName.length() - shieldNumber;
            for (int i = 0; i < length; i++) {
                sb.append("*");
            }
            userName = sb.append(userName.substring(length)).toString();
        } else if (userName.length() <= shieldNumber) {
            for (int i = 0; i < userName.length(); i++) {
                sb.append("*");
            }
            userName = sb.toString();
        }
        return userName;
    }
}

