package kz.kazpost.vpn.enums

enum class LabelsEnum(val ru: String, val t: String) {
    emsBag("Мешки и посылки \"EMS\"", "emsBag"),
    strBag("Мешки \"Сақтандыру\"", "strBag"),
    prvBag("Мешки и посылки \"Правительственные\"", "prvBag"),
    korBag("Мешки с корреспонденцией", "korBag"),
    gazeta("Мешки (пачки) с ППИ", "gazeta"),
    kgpo("Крупногабаритные почтовые отправления", "kgpo"),
    taraBag("Порожняя тара", "taraBag"),
    rpo("Посылки", "rpo"),
    otherBag("Прочие", "otherBag"),
    packetList("Мешки \"Международный\"", "packetList"),
    rpoCarefull("Посылки с отметкой «Осторожно»","rpoCarefull"),
    rpoEconom("Посылки с отметкой «эконом»", "rpoEconom"),
    ppiBag("Мешки (пачки) с ППИ", "ppiBag")
}