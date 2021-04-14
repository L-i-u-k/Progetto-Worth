package Progettoreti.server;

import java.util.ArrayList;
import java.util.Objects;

public class Card {
    final String nomeCard;
    private String descrizioneCard;
    final String statoCorrenteCard;

    ArrayList<String> history;

    public Card(String nomeCard) {
        this.nomeCard = nomeCard;
        this.statoCorrenteCard = "CoseDaFare";
        history = new ArrayList<>();
        history.add("CoseDaFare");
    }

    public void setDescrizioneCard(String descrizioneCard) {
        this.descrizioneCard = descrizioneCard;
    }

    public String getNomeCard() {
        return nomeCard;
    }

    public String getStatoCorrenteCard() {
        return statoCorrenteCard;
    }

    public String getDescrizioneCard() {
        return descrizioneCard;
    }

    public String getInfo() {
        return "Nome Card: " + getNomeCard() + "£Descrizione: " + getStatoCorrenteCard() + "£Stato Corrente: " + getDescrizioneCard() + "£";
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(nomeCard, card.nomeCard);

    }

    @Override
    public int hashCode() {
        return Objects.hash(nomeCard);
    }


}
