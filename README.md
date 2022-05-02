# Programmeringsuppgift - Payment Service

## Uppgift:
Uppgiften går ut på att läsa in filer, läsa av datat från filen rad för rad samt hantera datan.

### Filens innehåll:
Alla filer har en öppningspost samt X-antal betalningsposter och i vissa fall en slutpost.

Olika filtyper har olika innehåll samt olika positioner i raden som utgör specifikt innehåll (T.ex. index 40-48 kan va datumet)

### Exempel på filtyp: Beställningsservice:

Öppningspost() {

    1: O - Posttyp (fastvärde)
    2-16: 5555 5555555555 - Kontonummer (5555 = clearing, 5555555555 = kontonummer)
    17-30: 4711,17 - Summa
    31-40: 4 - Antal (Antal Betalningsposter)
    41-48: 20110315 - Betalningsdatum (ÅÅÅÅMMDD)
    49-51: SEK - Valuta

}

Betalningspost(förstaRaden, tredjeRaden) {

    förstaRaden() {
    1: B - Posttyp
    2-15: 3000 - Belopp
    16-50: 1234567890 - Referens (Sträng)
    }

    tredjeRaden() {
    1: B - Posttyp
    2-15: 300,10- Belopp
    16-50: 3456789012 - Referens
}

## Strategi

### En enkel körning:

1. Se ifall mappen "files" har några filer i sig
2. Läs av filernas namn/filtyp
    1. Filtypen avgör vilket objekt som ska skapas! (Ett alternativ till detta hade kunnat att läsa av öppningspostens posttyp)
    2. Filens namn blir namnet på objektet som skapas av sagd fil. Detta hade även varit namnet filen hade sparats under i en databas
3. Läs in raderna för filer.
    1. Första raden innehåller meta-data om själva transaktionen som t.ex. kontonummer/betalningsdatumet. Denna rad kallas även för "öppningsposten"
       Läs in och validera all data (t.ex. Kolla så att datumet är ett faktiskt datum)
    2. Betalningsposterna följer alltid Öppningsposten och håller in informationen om själva transaktionerna som har skett. T.ex Hur mycket individuella köp har gjorts för samt deras referrensnummer:
        1. Läs in och validera all data (t.ex. Kolla så att summan som kom in är ett decimaltal)
    3. När alla transaktioner har lästs in så kollar vi ifall det även finns en slutpost. Även denna post kommer att hålla meta-data om transaktionerna så som "öppningsposten"
        1. Läs in och validera all data (t.ex. Kolla så att summan som kom in är ett decimaltal)
    4. slutligen gör vi en sista koll för att se ifall total-summan stämmer överens med dem individuella transaktionernas summa.
4. När raderna har lästs in och presenterats i konsolen och skriver in ett inlägg till "/processed"
   1. "/confirmed_payments" ifall både valideringen har gått igenom samt summorna motsvarar varandra.
   1. "/failed_payments" ifall både valideringen har gått igenom MED summorna INTE motsvarar varandra.
5. Process Terminated

### Vid Fel:
- Ifall filen av någon anledning inte klarar av en/flera av valideringarna så kommer stacktrace:et presenterats i konsolen och programmet avslutas.


### Validering
Jag valde att begränsa scope:et till vilken utsträckning valideringen bör ske dåm


### Vidareutveckling
För att lägga till filtyper så ska det räcka med att skapa en klass för en ny filtyp med inläsning och validering hos den unika filen då positionerna/variablerna är unika för varje fil.


### Antaganden
- Hantera data = Spara ner datat mappat till rätt variabel in till en fil/databas
- Validera så att datat stämmer överens med dataypen


#Reflektion

Generellt sätt så har jag inte så svårt med att förhålla mig till konventioner där dem existerar (som i en befintlig kodbas). Däremot så har jag känt att det har varit en utav de svårare momenten för mig i detta projekt. Det är vad jag planerar på att förbättra inom min tid som junior utvecklare!

### Idéer för framtiden
- Koppla på hibernate(JPA) och lagra datat i en databas istället för en fil (Anledningen till att jag inte gjorde de va för att kunna visa på filhanterings-tekniker)
- Skapa en abstract class som håller i de gemensamma funktionerna som ligger i modellerna (filformaten)
- utöka valideringen. (t.ex. se så att emottagna filer är av rätt iso-standard)
- CI/CD med github-actions för ett bättre utvecklings-flöde och att tester kan köras.
- Städa try/catch flödet, (throw early catch late) 

## Vad hände med interface:t!?
Jag har sedan början av uppgiften haft svårt att förstå på vilket sätt som PaymentReceiver bör impleneteras.
Anledning - startPaymentBundle(Date paymentDate) - Finns bara datum i Betalningsservice.

Så jag valde att arbeta runt den interfacet och försöka visa på så mycket som möjligt på andra fronter.

