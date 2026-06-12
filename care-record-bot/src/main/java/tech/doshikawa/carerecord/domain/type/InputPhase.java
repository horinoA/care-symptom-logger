package tech.doshikawa.carerecord.domain.type;

/**
 * LINEからの入力状態（フェーズ）と、各ステップの画面を定義するEnum。
 * ステートマシンの現在地を表す。
 */
public enum InputPhase {
    /** 1枚目：症状の大カテゴリ・具体症状の選択を待っている状態（初期状態） */
    WAITING_FOR_SYMPTOM,

    /** 1枚目下部：「症状の入力を続けますか？」の選択（はい / 次のステップへ）を待っている状態 */
    WAITING_FOR_LOOP_CHOICE,

    /** 2枚目：カレンダー（日時ピッカー）の入力を待っている状態 */
    WAITING_FOR_DATE,

    /** 2枚目：時間帯（朝・昼・夕方・夜・深夜）の選択を待っている状態 */
    WAITING_FOR_TIMEZONE,

    /** 2枚目：経過時間（30分まで〜1日以上）の選択を待っている状態 */
    WAITING_FOR_DURATION,

    /** 3枚目：「誰の症状ですか？」の選択を待っている状態 */
    WAITING_FOR_WHO,

    /** 3枚目：「誰に向けた行動ですか？」の選択を待っている状態（「困った」カテゴリのみ突入） */
    WAITING_FOR_TO_WHO,

    /** 4枚目：「メモなしで保存」か「メモ追加保存」かの選択を待っている状態 */
    WAITING_FOR_SAVE_OR_MEMO,

    /** 4枚目：LINEの通常キーボードからの「メモ（自由テキスト）」の入力を待っている状態 */
    WAITING_FOR_MEMO_TEXT,

    /** 4枚目右下：メモ追加後の「最終保存確認」の選択を待っている状態 */
    WAITING_FOR_FINAL_CONFIRM;

    /**
     * DBに保存されている文字列からEnumを安全に取得する。
     * * @param phaseName 文字列のフェーズ名
     * @return 該当するInputPhase
     * @throws IllegalArgumentException 未知のフェーズ名が渡された場合
     */
    public static InputPhase fromString(String phaseName) {
        try {
            return InputPhase.valueOf(phaseName);
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new IllegalArgumentException("問答無用でエラー: 未知のフェーズ名です -> " + phaseName, e);
        }
    }

    /**
     * このフェーズが「自由テキスト（LINEのキーボード入力）」を待っている状態か判定する。
     * Service層で「Postback以外のテキストメッセージ」が飛んできた時のルーティングに大活躍する。
     * * @return テキスト入力を待っている場合は true
     */
    public boolean expectsText() {
        return this == WAITING_FOR_MEMO_TEXT;
    }

    /**
     * このフェーズが「初期状態（最初の画面）」か判定する。
     * * @return 初期状態の場合は true
     */
    public boolean isInitialPhase() {
        return this == WAITING_FOR_SYMPTOM;
    }
}