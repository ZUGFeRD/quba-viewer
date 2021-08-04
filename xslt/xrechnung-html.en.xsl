<!DOCTYPE HTML><html xmlns:i18n="https://github.com/ZUGFeRD/quba-viewer/blob/master/doc/xslt-i18n.md" xmlns:xr="urn:ce.eu:en16931:2017:xoev-de:kosit:standard:xrechnung-1" xmlns:xrv="http://www.example.org/XRechnung-Viewer" xmlns:xs="http://www.w3.org/2001/XMLSchema" lang="de">
   <head>
      <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
      <meta charset="UTF-8">
      <title>XRechnung</title>
      <meta name="viewport" content="width=device-width, user-scalable=yes, initial-scale=1.0">
      <style>


                    /* Grundformatierung ********************************************/

                    *,
                    *:after,
                    *:before
                    {
                    box-sizing: border-box;
                    -moz-box-sizing: border-box;
                    }

                    .clear:after
                    {
                    content: ".";
                    clear: both;
                    display: block;
                    visibility: hidden;
                    height: 0;
                    }

                    html,
                    body
                    {
                    height: 100%;
                    min-width: 320px;
                    margin: 0;
                    padding: 0;
                    color: #000;
                    font-size: 14px;
                    }

                    body
                    {
                    overflow-y: scroll;
                    background-color: rgba(4, 101, 161, 0.08);
                    }

                    h4
                    {
                    color: inherit;
                    font-size: inherit;
                    margin-bottom: 0.5rem;
                    }


                    /* Grundaufbau *************************************************/

                    .menue
                    {
                    position: relative;
                    z-index: 2000;
                    background-color: #000;
                    margin-bottom: 30px;
                    }

                    .innen
                    {
                    max-width: 1080px;
                    margin: 0 auto;
                    padding: 0 2%;
                    }


                    /* Formatierungen *************************************************/

                    .color2
                    {
                    color: rgba(0, 0, 0, 0.6);
                    }

                    .schwarz
                    {
                    color: #555 !important;
                    }

                    .normal
                    {
                    font-weight: normal;
                    }

                    .bold
                    {
                    font-weight: bold;
                    }

                    .abstandUnten
                    {
                    margin-bottom: 5px;
                    }

                    .abstandUntenKlein
                    {
                    margin-bottom: 10px;
                    }

                    .noPaddingTop
                    {
                    padding-top: 0 !important;
                    }

                    .ausrichtungRechts
                    {
                    text-align: right;
                    }


                    /* Menü ********************************************************/

                    button
                    {
                    position: relative;
                    font-family: serif;
                    padding-top: 15px;
                    padding-left: 0;
                    padding-right: 0;
                    margin-right: 2%;
                    }

                    .btnAktiv
                    {
                    font-size: 22px;
                    color: #ffb619;
                    height: 50px;
                    outline: none;
                    border: none;
                    background: none;
                    }

                    .btnAktiv:after
                    {
                    content: "";
                    display: block;
                    position: absolute;
                    top: 50px;
                    left: 50%;
                    z-index: 10;
                    font-size: 0;
                    line-height: 0;
                    height: 0;
                    padding: 0;
                    margin: 0;
                    transform: translateX(-50%);
                    border: 15px solid #000;
                    border-right-color: transparent;
                    border-bottom-color: transparent;
                    border-left-color: transparent;
                    }

                    .btnInaktiv,
                    .tab
                    {
                    font-size: 22px;
                    color: #fff;
                    height: 50px;
                    z-index: 0;
                    outline: none;
                    border: none;
                    background: none;
                    transition: color 0.3s ease;
                    }

                    .btnInaktiv:hover,
                    .tab:hover
                    {
                    color: #ffb619;
                    cursor: pointer;
                    }

                    .divHide
                    {
                    display: none;
                    }

                    /* Content *********************************************************************/

                    .inhalt
                    {
                    font-family: sans-serif;
                    margin-bottom: 30px;
                    }

                    .haftungausschluss
                    {
                    color: #000;
                    text-align: center;
                    padding: 7px;
                    margin-bottom: 30px;
                    width: 100%;
                    border: 1px solid #ffb619;
                    background-color: #fff;
                    }

                    .box
                    {
                    position: relative;
                    display: table-cell;
                    padding: 0;
                    border: 1px solid rgba(4, 101, 161, 0.2);
                    background-color: #fff;
                    }

                    .subBox
                    {
                    border-top: none;
                    width: 50%;
                    }

                    .subBox:last-child
                    {
                    border-left: none;
                    }

                    .first > .boxzeile > .subBox
                    {
                    border-top: 1px solid rgba(4, 101, 161, 0.2) !important;
                    }

                    .boxtitel
                    {
                    display: inline-block;
                    background-color: #0465A1;
                    padding: 7px 10px;
                    color: #fff;
                    font-weight: bold;
                    }

                    .boxBorderTop
                    {
                    border-top: none;
                    }

                    .boxBorderLeft
                    {
                    border-left: none;
                    }

                    .boxtitelSub
                    {
                    color: #000;
                    background-color: rgba(4, 101, 161, 0.1);
                    border-right: 1px solid rgba(4, 101, 161, 0.2);
                    border-bottom: 1px solid rgba(4, 101, 161, 0.2);
                    }

                    .boxinhalt
                    {
                    padding: 15px 20px;
                    }

                    .boxtabelle
                    {
                    display: table;
                    width: 100%;
                    }

                    .borderSpacing
                    {
                    border-spacing: 0 5px;
                    }

                    .boxabstandtop
                    {
                    margin-top: 30px;
                    }

                    .boxzeile
                    {
                    display: table-row;
                    }

                    .boxzeile .box:last-child
                    {
                    margin-bottom: 0;
                    }

                    .boxdaten
                    {
                    display: table-cell;
                    padding: 5px 0;
                    vertical-align: middle;
                    height: 38px;
                    /*
                    -ms-word-break: break-all;
                    word-break: break-all;
                    word-break: break-word;
                    -webkit-hyphens: auto;
                    -moz-hyphens: auto;
                    hyphens: auto;
                    */
                    }

                    .boxdaten.wert
                    {
                    padding: 5px 10px;
                    }

                    .boxcell
                    {
                    display: table-cell;
                    }

                    .boxdatenBlock
                    {
                    display: block;
                    padding: 3px 0;
                    /*
                    -ms-word-break: break-all;
                    word-break: break-all;
                    word-break: break-word;
                    -webkit-hyphens: auto;
                    -moz-hyphens: auto;
                    hyphens: auto;
                    */
                    }

                    .noBreak
                    {
                    -ms-word-break: keep-all;
                    word-break: keep-all;
                    word-break: keep-all;
                    -webkit-hyphens: none;
                    -moz-hyphens: none;
                    hyphens: none;
                    }

                    .boxabstand
                    {
                    display: table-cell;
                    width: 30px;
                    }

                    .legende
                    {
                    color: rgba(0, 0, 0, 0.6);
                    width: 170px;
                    font-size: 13px;
                    line-height: 16px;
                    padding-right: 5px;
                    }

                    .wert
                    {
                    background-color: rgba(4, 101, 161, 0.03);
                    }

                    .boxtabelleEinspaltig
                    {
                    width: 49%;
                    }

                    .boxtabelleZweispaltig,
                    .boxtabelleDreispaltig
                    {
                    width: 100%;
                    }

                    .box5050
                    {
                    width: 50%;
                    }

                    .boxEinspaltig
                    {
                    width: 100%;
                    }

                    .boxZweispaltig
                    {
                    width: 48.5%;
                    }

                    .boxSpalte1 {
                    width: 50%;
                    }

                    .boxSpalte2 {
                    width: 50%;
                    padding-left: 20px;
                    }

                    .paddingLeft {
                    padding-left: 0.1em;
                    }

                    .noPadding {
                    padding-top: 0 !important;
                    padding-bottom: 0 !important;
                    }

                    .rechnungsZeile
                    {
                    display: table-row;
                    }

                    .rechnungsZeile .boxdaten
                    {
                    height: auto;
                    }

                    .rechnungSp1
                    {
                    width: 65%;
                    font-size: 16px;
                    }

                    .rechnungSp2
                    {
                    width: 10%;
                    }

                    .rechnungSp3
                    {
                    width: 25%;
                    font-size: 16px;
                    text-align: right;
                    }

                    .detailSp1,
                    .detailSp2
                    {
                    width: 50%;
                    }

                    .detailSp2
                    {
                    text-align: right;
                    }

                    .line1Bottom
                    {
                    border-bottom: 1px solid #000;
                    }

                    .line1BottomLight
                    {
                    padding-bottom: 5px;
                    border-bottom: 1px solid #f0f0f0;
                    margin-bottom: 5px;
                    }

                    .line2Bottom
                    {
                    border-bottom: 2px solid #000;
                    }

                    .paddingTop
                    {
                    padding-top: 10px;
                    }

                    .paddingBottom
                    {
                    padding-bottom: 10px;
                    }

                    .grund
                    {
                    font-size: 16px;
                    display: block;
                    width: 100%;
                    padding: 0 20px 15px 20px;
                    }

                    .grundDetail
                    {
                    display: block;
                    width: 100%;
                    padding: 0 20px 15px 20px;
                    }

                    /* Übersichtformatierungen */
                    #uebersichtLastschrift.box,
                    #uebersichtUeberweisung.box
                    {
                    border-top: none;
                    }

                    #uebersichtUeberweisung.box
                    {
                    border-left: none;
                    }


                    /* Formatierungen Detailseite */

                    .detailsSpalte1,
                    .detailsSpalte2
                    {
                    width: 30%;
                    float: left;
                    font-size: 90%;
                    line-height: 115%;
                    margin-right: 5%;
                    }

                    .detailsSpalte3
                    {
                    width: 30%;
                    float: left;
                    font-size: 90%;
                    line-height: 115%;
                    }

                    .detailsSpalte1 .legende,
                    .detailsSpalte2 .legende,
                    .detailsSpalte3 .legende
                    {
                    width: 145px;
                    }

                    .titelPosition
                    {
                    font-size: 17px;
                    font-weight: bold;
                    }


                    /* Laufzettelformatierungen */
                    #laufzettelHistorie .boxtabelle:not(:nth-child(2))
                    {
                    border-top: 1px solid rgba(4, 101, 161, 0.2);
                    padding-top: 10px;
                    margin-top: 10px;
                    }


                    /* 1023px und kleiner ************************************************/

                    @media screen and (max-width : 1023px) {

                    .box
                    {
                    display: block;
                    width: 100%;
                    margin-bottom: 20px;
                    }

                    .boxabstandtop
                    {
                    margin-top: 15px;
                    }

                    .subBox:first-child
                    {
                    margin-bottom: 0 !important;
                    }

                    .subBox:last-child
                    {
                    border-left: 1px solid rgba(4, 101, 161, 0.2);
                    }

                    .first > .boxzeile > .subBox
                    {
                    border-top: none !important;
                    }

                    .first > .boxzeile > .subBox:first-child
                    {
                    border-top: 1px solid rgba(4, 101, 161, 0.2) !important;
                    }

                    .first > .boxzeile
                    {
                    margin-bottom: 0;
                    }

                    #uebersichtUeberweisung.box
                    {
                    border-left: 1px solid rgba(4, 101, 161, 0.2);
                    }

                    #uebersichtLastschrift.box
                    {
                    margin-bottom: 0;
                    }

                    .boxzeile
                    {
                    display: block;
                    margin-bottom: 5px;
                    }

                    .boxzeile:after
                    {
                    visibility: hidden;
                    display: block;
                    font-size: 0;
                    content: " ";
                    clear: both;
                    height: 0;
                    }

                    #details > .boxtabelle > .boxzeile
                    {
                    margin-bottom: 0px;
                    }

                    .boxcell
                    {
                    display: block;
                    }

                    .boxcell:last-child
                    {
                    margin-top: 20px;
                    }

                    .boxZweispaltig
                    {
                    width: 100%;
                    }

                    .legende
                    {
                    display: block;
                    float: left;
                    width: 170px;
                    padding: 5px 0;
                    height: auto;
                    }

                    .wert
                    {
                    display: block;
                    float: left;
                    width: calc(100% - 170px);
                    padding: 11px 10px !important;
                    line-height: 1.3;
                    min-height: 38px;
                    height: auto;
                    }

                    .boxdaten .legende
                    {
                    height: auto;
                    }

                    .rechnungsZeile .boxdaten
                    {
                    padding: 5px 0;
                    }

                    .boxabstand
                    {
                    display: none;
                    }

                    .boxtabelleEinspaltig {
                    width: 100%;
                    }

                    .boxSpalte1 {
                    display: block;
                    width: auto;
                    }

                    .boxSpalte2 {
                    display: block;
                    width: auto;
                    padding-left: 0px;
                    margin-top: 1.2rem;
                    }

                    .detailsSpalte1,
                    .detailsSpalte2,
                    .detailsSpalte3
                    {
                    width: 100%;
                    float: none;
                    padding-right: 0px;
                    }

                    .detailsSpalte2,
                    .detailsSpalte3
                    {
                    margin-top: 15px;
                    }

                    .detailsSpalte2,
                    .detailsSpalte3
                    {
                    margin-top: 10px;
                    }

                    .tableNumberAlignRight
                    {
                    display: block;
                    width: 130px;
                    text-align: right;
                    }
                    }


                    /* 800px und kleiner ************************************************/

                    @media screen and (max-width : 800px) {

                    button
                    {
                    padding-top: 10px;
                    }

                    .btnAktiv,
                    .btnInaktiv,
                    .tab
                    {
                    font-size: 20px;
                    height: 40px;
                    }

                    .btnAktiv:after
                    {
                    top: 40px;
                    }

                    .rechnungSp1
                    {
                    width: 55%;
                    font-size: 15px;
                    }

                    .rechnungSp2
                    {
                    width: 10%;
                    }

                    .rechnungSp3
                    {
                    width: 35%;
                    text-align: right;
                    font-size: 15px;
                    }

                    .grund
                    {
                    font-size: 15px;
                    }
                    }

                    /* 450px und kleiner ************************************************/

                    @media screen and (max-width : 450px)
                    {

                    html,
                    body
                    {
                    font-size: 12px;
                    }

                    .menue
                    {
                    margin-bottom: 20px;
                    }

                    button
                    {
                    padding-top: 5px;
                    }

                    .btnAktiv,
                    .btnInaktiv,
                    .tab
                    {
                    font-size: 17px;
                    height: 35px;
                    }

                    .btnAktiv:after
                    {
                    top: 35px;
                    }

                    .legende
                    {
                    font-size: 12px;
                    width: 100%;
                    }

                    .wert
                    {
                    font-size: 12px;
                    line-height: 1.3;
                    width: 100%;
                    margin-bottom: 10px
                    }

                    .boxzeile
                    {
                    margin-bottom: 0px
                    }

                    .boxdaten
                    {
                    height: auto;
                    }

                    .haftungausschluss
                    {
                    margin-bottom: 20px;
                    }

                    .boxinhalt
                    {
                    margin-top: 0px;
                    }

                    .boxabstandtop
                    {
                    margin-top: 20px;
                    }

                    .boxtitel
                    {
                    padding: 7px 8px;
                    }

                    .box
                    {
                    margin-bottom: 10px;
                    padding: 0;
                    }

                    .boxabstandtop
                    {
                    margin-top: 10px;
                    }

                    .boxdaten,
                    .boxdatenBlock
                    {
                    padding: 2px 0;
                    }

                    .rechnungSp1
                    {
                    width: 50%;
                    font-size: inherit;
                    }

                    .rechnungSp2
                    {
                    width: 15%;
                    }

                    .rechnungSp3
                    {
                    width: 35%;
                    font-size: inherit;
                    text-align: right;
                    }

                    .grund
                    {
                    font-size: inherit;
                    }

                    .titelPosition
                    {
                    font-size: 15px;
                    }

                    .abstandUnten
                    {
                    margin-bottom: 5px;
                    }

                    .detailsSpalte1,
                    .detailsSpalte2,
                    .detailsSpalte3
                    {
                    font-size: inherit;
                    line-height: inherit;
                    }
                    }

                    /* 380px und kleiner ************************************************/

                    @media screen and (max-width : 380px) {

                    html,
                    body
                    {
                    font-size: 11px;
                    line-height: 100%;
                    }

                    .btnAktiv,
                    .btnInaktiv,
                    .tab
                    {
                    font-size: 15px;
                    }

                    .boxdaten
                    .boxdatenBlock
                    {
                    padding: 2px 0;
                    }

                    .boxinhalt
                    {
                    margin-top: 0px;
                    }

                    .boxtitel
                    {
                    padding: 5px 7px;
                    }
                    }


                </style>
   </head>
   <body>
      <form>
         <div class="menue">
            <div class="innen"><button type="button" class="tab" id="menueUebersicht" onclick="show(this);">Aperçu</button><button type="button" class="tab" id="menueDetails" onclick="show(this);">Positionen</button><button type="button" class="tab" id="menueZusaetze" onclick="show(this)">Informationen</button><button type="button" class="tab" id="menueAnlagen" onclick="show(this)">Pièces jointes</button><button type="button" class="tab" id="menueLaufzettel" onclick="show(this)">Historique de traitement</button></div>
         </div>
      </form>
      <div class="inhalt">
         <div class="innen">
            <div id="uebersicht" class="divShow">
               <div class="haftungausschluss">Nous n'assumons aucune responsabilité quant à l'exactitude des données.</div>
               <div class="boxtabelle boxtabelleZweispaltig">
                  <div class="boxzeile">
                     <div id="uebersichtKaeufer" class="box boxZweispaltig">
                        <div id="BG-7" title="BG-7" class="boxtitel">Informations sur l'acheteur</div>
                        <div class="boxtabelle boxinhalt borderSpacing">
                           <div class="boxzeile">
                              <div class="boxdaten legende">:
                                 </div>
                              <div id="BT-10" title="BT-10" class="boxdaten wert">991-01484-64</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Nom:
                                 </div>
                              <div id="BT-44" title="BT-44" class="boxdaten wert">Franz Müller</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Rue / Numéro de maison:
                                 </div>
                              <div id="BT-50" title="BT-50" class="boxdaten wert">teststr.12</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Boîte postale:
                                 </div>
                              <div id="BT-51" title="BT-51" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Supplément d'adresse:
                                 </div>
                              <div id="BT-163" title="BT-163" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Code postal:
                                 </div>
                              <div id="BT-53" title="BT-53" class="boxdaten wert">55232</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Lieu:
                                 </div>
                              <div id="BT-52" title="BT-52" class="boxdaten wert">Entenhausen</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Région:
                                 </div>
                              <div id="BT-54" title="BT-54" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Pays:
                                 </div>
                              <div id="BT-55" title="BT-55" class="boxdaten wert">DE</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Identifiant:
                                 </div>
                              <div id="BT-46" title="BT-46" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Schéma de l'Identifiant:
                                 </div>
                              <div id="BT-46-scheme-id" title="BT-46-scheme-id" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Nom:
                                 </div>
                              <div id="BT-56" title="BT-56" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Téléphone:
                                 </div>
                              <div id="BT-57" title="BT-57" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Adresse électronique:
                                 </div>
                              <div id="BT-58" title="BT-58" class="boxdaten wert"></div>
                           </div>
                        </div>
                     </div>
                     <div class="boxabstand"></div>
                     <div id="uebersichtVerkaeufer" class="box boxZweispaltig">
                        <div id="BG-4" title="BG-4" class="boxtitel">Informations sur le vendeur</div>
                        <div class="boxtabelle boxinhalt borderSpacing">
                           <div class="boxzeile">
                              <div class="boxdaten legende"></div>
                              <div class="boxdaten wert" style="background-color: white;"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Nom de la société:
                                 </div>
                              <div id="BT-27" title="BT-27" class="boxdaten wert">Test company</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Rue / Numéro de maison:
                                 </div>
                              <div id="BT-35" title="BT-35" class="boxdaten wert">teststr</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Boîte postale:
                                 </div>
                              <div id="BT-36" title="BT-36" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Supplément d'adresse:
                                 </div>
                              <div id="BT-162" title="BT-162" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Code postal:
                                 </div>
                              <div id="BT-38" title="BT-38" class="boxdaten wert">55232</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Lieu:
                                 </div>
                              <div id="BT-37" title="BT-37" class="boxdaten wert">teststadt</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Région:
                                 </div>
                              <div id="BT-39" title="BT-39" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Code pays:
                                 </div>
                              <div id="BT-40" title="BT-40" class="boxdaten wert">DE</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Identifiant:
                                 </div>
                              <div id="BT-29" title="BT-29" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Schéma de l'Identifiant:
                                 </div>
                              <div id="BT-29-scheme-id" title="BT-29-scheme-id" class="boxdaten wert"></div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Nom:
                                 </div>
                              <div id="BT-41" title="BT-41" class="boxdaten wert">Hans Test</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Téléphone:
                                 </div>
                              <div id="BT-42" title="BT-42" class="boxdaten wert">+49123456789</div>
                           </div>
                           <div class="boxzeile">
                              <div class="boxdaten legende">Adresse électronique:
                                 </div>
                              <div id="BT-43" title="BT-43" class="boxdaten wert">test@example.org</div>
                           </div>
                        </div>
                     </div>
                  </div>
               </div>
               <div class="boxtabelle boxabstandtop boxtabelleZweispaltig">
                  <div class="boxzeile">
                     <div id="uebersichtRechnungsinfo" class="box box1v2">
                        <div class="boxtitel">Détails de la facturation</div>
                        <div class="boxtabelle boxinhalt">
                           <div class="boxcell boxZweispaltig">
                              <div class="boxtabelle borderSpacing">
                                 <div class="boxzeile">
                                    <div class="boxdaten legende">Informations sur le vendeur:
                                       </div>
                                    <div id="BT-1" title="BT-1" class="boxdaten wert">123</div>
                                 </div>
                                 <div class="boxzeile">
                                    <div class="boxdaten legende">Date de f