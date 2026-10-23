public class Vedec {

    private final String meno;
    private boolean dialogDokonceny = false;

    public Vedec(String meno) {
        this.meno = meno;
    }

    public String getMeno() {
        return meno;
    }

    public boolean isDialogDokonceny() {
        return dialogDokonceny;
    }

    public void setDialogDokonceny(boolean dialogDokonceny) {
        this.dialogDokonceny = dialogDokonceny;
    }

    // Prvý rozhovor je dlhší, ďalšie sú už len stručné pripomenutie.
    // Kód zámku sa mení v každej hre, preto ho dostane ako parameter.
    public String hovor(int kodZamku) {
        if (!dialogDokonceny) {
            dialogDokonceny = true;
            return "Ďakabohu, žiješ! Chladiaci systém experimentu zlyhal a bezpečnostný systém zamkol celý komplex.\n"
                    + "Bezpečnostný zámok sa dá deaktivovať kódom na termináli. Ten kód je " + kodZamku + ", ale zadať ho musíš ty.\n"
                    + "Servisný panel tu v miestnosti sa otvára skrutkovačom. Vo vnútri je prístupová karta a chemická vzorka.\n"
                    + "Chladiaci systém v dekontaminačnej komore opravíš tou vzorkou. Potom už len overíš kartu a zadáš finálny kód.\n"
                    + "Ten finálny kód nepoznám, ale terminál ho po odomknutí určite zobrazí. Ponáhľaj sa, nemáme veľa času!";
        }
        return "Ak si zabudol kód, je to: " + kodZamku + ". Potom musíš opraviť chladiaci systém a dostať sa k núdzovému východu. Vzorka, karta a finálny kód!";
    }
}
