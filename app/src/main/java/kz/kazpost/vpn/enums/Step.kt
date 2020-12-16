package kz.kazpost.vpn.enums

enum class Step(val title: String) {
    ACCEPT_INVOICE("О - накладная (прием)"),
    ACCEPT_START("Прием завершен"),
    ACCEPT_SCAN("Подтверждение РПО и емкостей"),
    ACCEPT_SCAN2("Сканирование недостач"),
    ACCEPT_ACT("Список созданных актов"),
    ACCEPT_BG("Список принятых РПО и емкостей"),
    ACCEPT_END("Прием посылок завершен"),
    TRANSFER_SCAN("Формирование О-накладной"),
    TRANSFER_SCAN2("Добавление недостач"),
    TRANSFER_BG("Список сформированных РПО"),
    TRANSFER_INVOICE("Накладная (передача)"),
    TRANSFER_END("Посылки переданы")
}