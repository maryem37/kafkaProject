%%{init: {
  "theme": "dark",
  "flowchart": {"curve": "stepBefore", "htmlLabels": true},
  "themeVariables": {
    "background": "#0b1220",
    "primaryColor": "#0f172a",
    "primaryTextColor": "#e5e7eb",
    "primaryBorderColor": "#334155",
    "lineColor": "#94a3b8",
    "secondaryColor": "#111827",
    "tertiaryColor": "#0b1220"
  }
}}%%
flowchart LR

  %% ===== Styles =====
  classDef edge fill:#0f172a,stroke:#334155,color:#e5e7eb,stroke-width:1.5px;
  classDef svc fill:#0b1220,stroke:#60a5fa,color:#e5e7eb,stroke-width:2px;
  classDef infra fill:#0f172a,stroke:#f59e0b,color:#e5e7eb,stroke-width:2px;
  classDef note fill:#0b1220,stroke:#334155,color:#cbd5e1,stroke-dasharray: 4 4;

  %% ===== Edge / Client =====
  subgraph EDGE["Edge"]
    direction LR
    U["s1"]
    FE["s2"]
    GW["s3"]

    U -->|"s16"| GW
    FE -->|"s17"| GW
  end

  %% ===== Microservices =====
  subgraph MS["Spring Boot Microservices"]
    direction TB
    B["s4"]
    O["s5"]
    I["s6"]
    P["s7"]
  end

  %% Gateway routing
  GW -->|"s18"| B
  GW -->|"s19"| O
  GW -->|"s20"| I
  GW -->|"s21"| P

  %% ===== Kafka =====
  subgraph K["Kafka"]
    direction TB
    KAF["s8"]
    T1["s24"]
    T2["s25"]
    T3["s26"]
    T4["s27"]
  end

  %% Producers -> Kafka (async)
  B -. "publish" .-> T1
  O -. "publish" .-> T2
  I -. "publish" .-> T3
  P -. "publish" .-> T4

  %% Topics -> Consumers (async)
  T1 -. "consume" .-> O
  T2 -. "consume" .-> I
  T2 -. "consume" .-> P
  T3 -. "consume" .-> B

  %% Topics are stored/handled by broker
  T1 --- KAF
  T2 --- KAF
  T3 --- KAF
  T4 --- KAF

  %% ===== Data / External =====
  subgraph DATA["Data + External"]
    direction TB
    DB["s9"]
    MT["s10"]
  end

  %% Apply styles (classes)
  class U,FE,GW edge;
  class B,O,I,P svc;
  class KAF,DB,MT infra;
  class T1,T2,T3,T4 note;

  %% DB usage (sync)
  B -->|"s22"| DB
  I -->|"s23"| DB
  P -->|"s24"| DB

  %% Payment email
  P -->|"s25"| MT

  %% Visual separation hints
  KAF --- DB