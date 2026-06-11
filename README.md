# 認知症介護 症状記録アプリ（LINE Bot） 要件定義書
# care-symptom-logger
認知症患者の家族が症状のファクトを問答無用ぽちぽちでLINEbotから投稿し保存できる

## 1. プロジェクトの背景と目的

### 1.1 背景
認知症の家族を介護する際、医療機関やケアマネージャー等への「正確な症状の共有」が適切なケアプラン作成・医療保護入院などの手続きにおいて不可欠となる。しかし、パニック状態や疲労困憊にある家族が、初対面の専門家に対して客観的かつ時系列に沿った事象の説明を行うことは極めて困難である。
本プロジェクトは、システム開発における「バグ報告・ログ設計」の知見を応用し、ITリテラシーが高くない一般の介護者でも、日常の「ファクト（5W1H）」を容易に記録・蓄積できる仕組みを提供するものである。

### 1.2 目的
* **介護者の負担軽減:** 専門家への説明という高いハードルを、日々の「問答無用ぽちぽち」入力によるログ蓄積で代替する。
* **専門家への「トリガー」提供:** 完璧な定性データではなく、医療・介護の専門家が的確なヒアリングを行うための「客観的な事実データ（発生頻度・状況）」を提供する。

## 4. 機能要件

### 4.1 初回登録・家系図設定（LIFF連携）
* 新規ユーザーに対し、LINEチャット上で初期設定を実施。
* 個人情報入力は避け、登場する家族関係（本人を中心とした3世代のジェノグラム相当）と、医療・介護の対象者（ケアマネ、医師など）のアイコンを登録する。

### 4.2 症状記録入力（LINE対話UI）
LINEのトークルームにて、ボットとの対話（クイックリプライ・カルーセル）を通じて以下の項目を記録する。
* **発症日時:** (必須) デフォルトは現在時刻、過去指定も可能。
* **発症者・対象者:** (必須) 家系図からタップで選択。
* **症状名カテゴリ:** (必須) 「困った」「記憶と言葉」「行動」「見当」の4カテゴリから具体的な症状をタップで選択。
* **ランク・感情:** (任意) 症状の程度（3段階）や、介護者自身の感情（怒り・不安など）。
* **メモ:** (任意) 256文字以内のフリーテキスト。

### 4.3 記録一覧・検索（LIFF）
* トークルームを遡る形での確認のほか、リッチメニューからWebビュー（LIFF）を開き、日付順・発症者別・症状別のリスト表示および全文検索を提供する。

### 4.4 記録のエクスポート（共有機能）
* 専門家との面談用資料作成のため、期間を指定して「症状記録テキスト出力」または「CSV出力」を生成する。
* 生成されたデータは、登録されたメールアドレス宛に送信、またはダウンロードリンクとして提供する。

### 4.5 相談フロー・ヘルプ機能
* 医療や介護への相談方法がわからないユーザーのため、適切な相談先（地域包括支援センター等）へ繋がるための案内フローを画像付きのチャットボットで提供する。

## 5. データモデル設計（ER図）

```mermaid
erDiagram
  symptonCategry ||--|{symptom  : "1の症状分類は複数の症状項目をもつ"
  peopleCare {
    integer id PK
    string relationship "続柄"
    boolean whoCheck "(誰が)選択時表示"
  }
  symptom {
    integer id PK
    integer symptonCatId FK
    string symptomItem "症状項目"
  }
  symptonCategry {
    integer id PK
    string symptonCatName "症状分類名"
  }
  feeling {
    integer id PK
    string emotionalname "説明"
  }
  
  registrant {
    bigint id PK "LINE_UserID(ハッシュ)"
    string eMail 
  }
  
  symptomArticle {
    bigint id PK
    bigint registrantId FK "登録者ID"
    datetime onsetTime "発症日時"
    integer whoIs FK "誰が(peopleCare)"
    integer toWho FK "誰に(peopleCare)"
    integer evaluation "評価(ランク)"
    integer feeling FK "感情"
    string memo "メモ"
  }
  symptomRec {
    bigint id PK
    bigint symptomArticleID FK
    integer symptomID FK
  }

  symptompeoplecare {
    bigint id PK
    bigint registrantId PK "登録者ID"
    integer peoplecreid
    boolean whoIscheck
    boolean toWhocheckß
    integer sortorder
  }
  
  symptomArticle ||--o{symptomRec  : "1の症状記事は0or複数の症状記録をもつ"
  symptomArticle ||--o{feeling  : "1の症状記事は0or複数のfeeling（感情）をもつ"
  symptomArticle ||--|| symptompeoplecare : "1の登録者は1登録者IDを持つ"
  registrant ||--o{symptomArticle :"1の登録者は0または複数の症状記事を持つ"
  symptom ||--|{symptomRec  : "1の症状項目は複数の症状記録をもつ"
