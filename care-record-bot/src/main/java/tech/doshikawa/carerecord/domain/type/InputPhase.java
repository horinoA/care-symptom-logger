package tech.doshikawa.carerecord.domain.type;

/**
 * LINEからの入力状態（フェーズ）と、各ステップの画面遷移を定義するEnum。
 * 最新の画面遷移図に完全準拠。
 */
public enum InputPhase {
    // --- 1枚目 ---
    /** 症状の大カテゴリ（困った、記憶と行動など）の選択を待っている状態（初期状態） */
    WAITING_FOR_SYMPTOM_CATEGORY,

    /** 選択されたカテゴリ内の、具体的な症状リストからの選択を待っている状態 */
    WAITING_FOR_SYMPTOM_DETAIL,

    /** 「症状の入力を続けますか？」の選択（はい / 次のステップへ）を待っている状態 */
    WAITING_FOR_LOOP_CHOICE,

    // --- 2枚目 ---
    /** カレンダー（日時ピッカー）の入力を待っている状態 */
    WAITING_FOR_DATE,

    /** 時間帯（朝・昼・夕方・夜・深夜）の選択を待っている状態 */
    WAITING_FOR_TIMEZONE,

    /** 経過時間（30分まで〜1日以上）の選択を待っている状態 */
    WAITING_FOR_DURATION,

    // --- 3枚目 ---
    /** 「誰の症状ですか？」の大枠の選択を待っている状態 */
    WAITING_FOR_WHO,

    /** 「誰の症状ですか？」のサブメニュー（義父母・祖父母世代など）の選択を待っている状態 */
    WAITING_FOR_WHO_SUB,

    /** 「誰に向けた行動ですか？」の選択を待っている状態（「困った」カテゴリのみ突入） */
    WAITING_FOR_TO_WHO,

    /** 「誰に向けた行動ですか？」のサブメニュー（医療従事者・他世代など）の選択を待っている状態 */
    WAITING_FOR_TO_WHO_SUB,

    // --- 4枚目 ---
    /** 「メモなしで保存」か「メモ追加保存」かの選択を待っている状態 */
    WAITING_FOR_SAVE_OR_MEMO,

    /** LINEの通常キーボードからの「メモ（自由テキスト）」の入力を待っている状態 */
    WAITING_FOR_MEMO_TEXT,

    /** メモ追加後の「最終保存確認」の選択を待っている状態 */
    WAITING_FOR_FINAL_CONFIRM;

    /**
     * DB等に保存されている文字列からEnumを安全に取得する。
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
     */
    public boolean expectsText() {
        return this == WAITING_FOR_MEMO_TEXT;
    }

    /**
     * このフェーズが「初期状態（最初の画面）」か判定する。
     */
    public boolean isInitialPhase() {
        return this == WAITING_FOR_SYMPTOM_CATEGORY;
    }
}