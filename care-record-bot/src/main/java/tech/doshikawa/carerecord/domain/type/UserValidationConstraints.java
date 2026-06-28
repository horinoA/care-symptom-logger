package tech.doshikawa.carerecord.domain.type;

public final class UserValidationConstraints {
    
    UserValidationConstraints(){}

    // 文字数制限
    public static final int MAX_COMMENT_LENGTH = 255;
    public static final String MAX_COMMENT_LENGTH_STR = "255";
    /**
     * 弾くための正規表現パターン
     * 
     * 1. 住所：都道府県名や「市区町村」の漢字、丁目・番地表現(号/階/ldk)などを大まかに検知
     * 2. 名前：一般的な「様」や、名前入力欄によくある「テスト」「サンプル」を検知
     * 3. 電話番号：桁数（2〜4桁-2〜4桁-3〜4桁）や、連続する数字を検知
     */
    // 住所っぽい単語（部分一致で弾くための否定先読み）
    public static final String NO_ADDRESS_PATTERN = "^(?!.*(都|道|府|県|市|区|町|村|丁目|番地|アパート|マンション|ビル)).*$";
    // 名前っぽい単語（「〜様」や、テスト用入力を弾く）
    public static final String NO_NAME_PATTERN = "^(?!.*(様|テスト|てすと|サンプル|さんぷる)).*$";
    // 電話番号っぽい数字の並び（ハイフンあり・なしの両方に対応）
    public static final String NO_PHONE_PATTERN = "^(?!.*(\\d{2,4}-\\d{2,4}-\\d{3,4}|\\d{10,11})).*$";


}
