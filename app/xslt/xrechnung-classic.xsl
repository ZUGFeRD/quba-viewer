<?xml version="1.0" encoding="UTF-8"?>
<!--
    xrechnung-classic.xsl
    Alternative Stage-2 (XR -> HTML) fuer den Quba-XRechnung-Viewer.

    Rendert das XR-Intermediate im klassischen deutschen Rechnungslayout
    (Ersatz-Ansicht zur technischen KoSIT-Visualisierung, waehlbar per Toggle).

    Gebaut allein aus docs/xrechnung-classic-layout-spec.md (Status VALIDIERT)
    und docs/xr-intermediate-field-map.md - Clean-Room (AD-005), ohne PDF24-Material.

    Konsumiert dasselbe XR-XML wie xrechnung-html.uni.xsl:
      Namespace  urn:ce.eu:en16931:2017:xoev-de:kosit:standard:xrechnung-1  (Prefix xr:)
      Wurzel     <xr:invoice>, jeder Feldknoten mit @xr:id (BT/BG-Nummer).
    Selektion primaer per @xr:id='BT-nnn' (robust gegen Vokabular-Aenderungen),
    Gruppen-Wrapper (xr:INVOICE_LINE, xr:VAT_BREAKDOWN, ...) per Elementname.

    Kompilieren:  xslt3 -xsl:xrechnung-classic.xsl -export:xrechnung-classic.sef.json -nogo
    Laufzeit fuehrt nur die .sef.json aus (saxon-js 2.4).
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xr="urn:ce.eu:en16931:2017:xoev-de:kosit:standard:xrechnung-1"
                xmlns:xrv="http://www.example.org/XRechnung-Viewer"
                exclude-result-prefixes="xsl xs xr xrv">

    <!-- Von der bestehenden Transform-Pipeline uebergebene Parameter (main.js).
         Das klassische Layout bringt deutsche Labels selbst mit und braucht
         i18n/isOrder nicht; sie werden nur deklariert, damit die uebergebenen
         stylesheetParams sauber gebunden werden. showIds blendet optional
         BT-Kennungen als title-Attribute ein (Debug-Hilfe). -->
    <xsl:param name="i18n" as="map(*)" select="map{}"/>
    <xsl:param name="isOrder" as="xs:boolean" select="false()"/>
    <xsl:param name="showIds" as="xs:boolean" select="false()"/>

    <xsl:output indent="yes" method="html" encoding="UTF-8"/>
    <xsl:decimal-format name="decimal" decimal-separator="," grouping-separator="." NaN=""/>

    <!-- ============================================================= -->
    <!-- Hilfsfunktionen (Formatierung deutsch)                        -->
    <!-- ============================================================= -->

    <!-- ISO-Datum JJJJ-MM-TT -> TT.MM.JJJJ (Rest unveraendert durchreichen).
         Alle Funktionen nehmen den ersten Knoten (($v)[1]), da ein BT im XR
         mehrfach vorkommen kann (z.B. zwei Kontaktstellen). -->
    <xsl:function name="xrv:date" as="xs:string">
        <xsl:param name="v"/>
        <xsl:variable name="s" select="normalize-space(string(($v)[1]))"/>
        <xsl:sequence select="if (matches($s,'^\d{4}-\d{2}-\d{2}'))
                                then format-date(xs:date(substring($s,1,10)),'[D01].[M01].[Y0001]')
                                else $s"/>
    </xsl:function>

    <!-- Waehrungscode -> Symbol (nur haeufige Codes; sonst Code selbst).
         Getrieben von BT-5, nicht fix Euro (Korpus enthaelt GBP). -->
    <xsl:function name="xrv:cursym" as="xs:string">
        <xsl:param name="c"/>
        <xsl:variable name="cc" select="xrv:codeonly($c)"/>
        <xsl:sequence select="if ($cc='EUR') then '&#8364;'
                              else if ($cc='GBP') then '&#163;'
                              else if ($cc='USD') then '$'
                              else $cc"/>
    </xsl:function>

    <!-- Rohbetrag -> 1.234,56 (leer bei Nicht-Zahl) -->
    <xsl:function name="xrv:money" as="xs:string">
        <xsl:param name="v"/>
        <xsl:variable name="n" select="number(normalize-space(string(($v)[1])))"/>
        <xsl:sequence select="if ($n = $n) then format-number($n,'###.##0,00','decimal') else ''"/>
    </xsl:function>

    <!-- Rohbetrag + Waehrung -> 1.234,56 EUR (geschuetztes Leerzeichen) -->
    <xsl:function name="xrv:moneyc" as="xs:string">
        <xsl:param name="v"/>
        <xsl:param name="cur"/>
        <xsl:variable name="m" select="xrv:money($v)"/>
        <xsl:sequence select="if ($m='') then '' else concat($m,'&#160;',xrv:cursym($cur))"/>
    </xsl:function>

    <!-- Menge deutsch (bis 4 Nachkommastellen, Trailing weg) -->
    <xsl:function name="xrv:qty" as="xs:string">
        <xsl:param name="v"/>
        <xsl:variable name="n" select="number(normalize-space(string(($v)[1])))"/>
        <xsl:sequence select="if ($n = $n) then format-number($n,'###.##0,####','decimal')
                              else normalize-space(string(($v)[1]))"/>
    </xsl:function>

    <!-- Ist der Wert eine Zahl != 0 ? (NaN-sicher: $n=$n ist bei NaN falsch) -->
    <xsl:function name="xrv:nz" as="xs:boolean">
        <xsl:param name="v"/>
        <xsl:variable name="n" select="number(normalize-space(string(($v)[1])))"/>
        <xsl:sequence select="$n = $n and $n != 0"/>
    </xsl:function>

    <!-- Codelisten-Aufloesung im XR liegt als "CODE (Beschreibung)" vor
         (Phase-3-Befund am echten Rendering). Zwei Extraktoren:
         codeonly -> fuehrender Code ("EUR (Euro)" -> "EUR", "K (...)" -> "K"),
         codetext -> Beschreibung in Klammern ("H87 (piece)" -> "piece").
         Ohne Klammern jeweils der ganze (getrimmte) Wert. -->
    <xsl:function name="xrv:codeonly" as="xs:string">
        <xsl:param name="v"/>
        <xsl:sequence select="normalize-space(replace(string(($v)[1]),'\s*\(.*$',''))"/>
    </xsl:function>

    <xsl:function name="xrv:codetext" as="xs:string">
        <xsl:param name="v"/>
        <xsl:variable name="s" select="normalize-space(string(($v)[1]))"/>
        <xsl:sequence select="if (matches($s,'\(.*\)'))
                                then normalize-space(replace($s,'^[^(]*\(([^)]*)\).*$','$1'))
                                else $s"/>
    </xsl:function>

    <!-- Prozentsatz deutsch ohne ueberfluessige Nullen: "19.00" -> "19", "7.5" -> "7,5" -->
    <xsl:function name="xrv:pct" as="xs:string">
        <xsl:param name="v"/>
        <xsl:variable name="n" select="number(normalize-space(string(($v)[1])))"/>
        <xsl:sequence select="if ($n = $n) then format-number($n,'0,##','decimal')
                              else normalize-space(string(($v)[1]))"/>
    </xsl:function>

    <!-- ============================================================= -->
    <!-- Globale Variablen                                             -->
    <!-- ============================================================= -->

    <!-- Rechnungswaehrung (BT-5) - alle Betraege damit formatieren. Nur den
         ISO-Code, da XR "EUR (Euro)" liefert. -->
    <xsl:variable name="cur" select="xrv:codeonly((//*[@xr:id='BT-5'])[1])"/>
    <!-- Buchungswaehrung (BT-6) - Fremdwaehrungsfall wenn != BT-5 -->
    <xsl:variable name="acccur" select="xrv:codeonly((//*[@xr:id='BT-6'])[1])"/>
    <!-- Traegt mindestens eine Position eine Positions-Bestellnummer (BT-132)? -->
    <xsl:variable name="hasPO"
                  select="boolean(//xr:INVOICE_LINE[.//*[@xr:id='BT-132'][normalize-space()!='']])"/>

    <!-- ============================================================= -->
    <!-- Wurzel                                                        -->
    <!-- ============================================================= -->

    <xsl:template match="/xr:invoice">
        <!-- Dokumenttitel aus Typcode (BT-3) ableiten. XR liefert "380 (Commercial
             invoice)" -> nur den fuehrenden Code auswerten. -->
        <xsl:variable name="tc" select="xrv:codeonly((//*[@xr:id='BT-3'])[1])"/>
        <xsl:variable name="title" select="
            if ($tc='380') then 'Rechnung'
            else if ($tc='381') then 'Gutschrift'
            else if ($tc='384') then 'Rechnungskorrektur'
            else if ($tc='389') then 'Selbst ausgestellte Rechnung'
            else if ($tc='751') then 'Rechnung (Buchungshilfe)'
            else if ($tc castable as xs:integer) then 'Rechnung'
            else if ($tc != '') then $tc
            else 'Rechnung'"/>

        <html lang="de">
            <head>
                <meta charset="UTF-8"/>
                <title><xsl:value-of select="$title"/></title>
                <meta name="viewport" content="width=device-width, initial-scale=1"/>
                <style>
    * { box-sizing: border-box; }
    body { font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
           font-size: 10.5pt; line-height: 1.4; color: #1a1a1a; background: #fff; margin: 0; }
    .invoice { max-width: 180mm; margin: 0 auto; padding: 6mm 4mm; }

    .head-row { display: flex; justify-content: space-between; gap: 12mm; margin-bottom: 6mm; }
    .col-left  { flex: 1 1 55%; }
    .col-right { flex: 1 1 45%; text-align: right; }

    .party .name { font-weight: 700; font-size: 13pt; margin-bottom: 1mm; }
    .blocktitle { font-weight: 700; color: #333; margin: 0 0 1mm 0; }
    .ln { margin: 0.4mm 0; }
    .lb { color: #555; }
    .vl { color: #1a1a1a; }
    .scheme { color: #777; font-size: 8.5pt; }

    table.meta { border-collapse: collapse; margin-left: auto; }
    table.meta td { padding: 0.4mm 0 0.4mm 5mm; vertical-align: top; }
    table.meta td.ml { color: #555; text-align: left; white-space: nowrap; }
    table.meta td.mv { text-align: right; font-variant-numeric: tabular-nums; }

    h1.doctitle { font-size: 17pt; font-weight: 700; margin: 4mm 0 4mm 0; }

    table.pos { width: 100%; border-collapse: collapse; margin-bottom: 4mm; }
    table.pos thead th { background: #eee; text-align: left; padding: 1.6mm 2mm;
                         border-bottom: 1.5px solid #999; font-size: 9.5pt; font-weight: 700; }
    table.pos th.r, table.pos td.r { text-align: right; white-space: nowrap; }
    table.pos td { padding: 1.6mm 2mm; border-bottom: 0.5px solid #ddd; vertical-align: top; }
    table.pos td.idx { white-space: nowrap; color: #555; }
    .po-cell { font-weight: 600; }
    .itemname { color: #1a1a1a; }
    .subln { color: #555; font-size: 8.5pt; margin-top: 0.6mm; }
    .unit, .cat { color: #555; font-size: 8.5pt; }

    .sums-wrap { display: flex; justify-content: flex-end; }
    table.sums { border-collapse: collapse; min-width: 78mm; }
    table.sums td { padding: 0.9mm 2mm; }
    table.sums td.sl { color: #333; text-align: left; }
    table.sums td.sv { text-align: right; font-variant-numeric: tabular-nums; white-space: nowrap; }
    table.sums tr.net-total td { border-top: 0.8px solid #bbb; }
    table.sums tr.due td { font-weight: 700; border: 1.2px solid #333; background: #f2f2f2; font-size: 11.5pt; }
    .vat-reason { text-align: right; color: #555; font-size: 9pt; margin-top: 1mm; }
    .acc-cur { text-align: right; color: #555; font-size: 9pt; margin-top: 1mm; }

    .section { margin-top: 5mm; }
    .section h2 { font-size: 11pt; font-weight: 700; margin: 0 0 1.5mm 0;
                  border-bottom: 0.5px solid #ccc; padding-bottom: 0.5mm; }
    .note-code { color: #777; font-size: 8.5pt; margin-right: 2mm; }
    .anlage-row { margin: 1mm 0; }
    .anlage-mime { color: #777; font-size: 8.5pt; }
    a { color: #1a4f8b; }

    @page { size: A4; margin: 18mm; }
    @media print {
        .invoice { max-width: none; margin: 0; padding: 0; }
        a { color: #1a1a1a; text-decoration: none; }
        table.pos thead { display: table-header-group; }
        .sums-wrap, .section, table.sums { break-inside: avoid; }
        body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }
    }
                </style>
            </head>
            <body>
                <div class="invoice">

                    <!-- ===== Kopf, Zeile 1: Verkaeufer (links) | Zahlung + Kontakt (rechts) ===== -->
                    <div class="head-row">
                        <div class="col-left party">
                            <xsl:call-template name="seller-block"/>
                        </div>
                        <div class="col-right">
                            <xsl:call-template name="payment-block"/>
                            <xsl:call-template name="seller-contact-block"/>
                        </div>
                    </div>

                    <!-- ===== Kopf, Zeile 2: Empfaenger (links) | Metadaten (rechts) ===== -->
                    <div class="head-row">
                        <div class="col-left party">
                            <xsl:call-template name="buyer-block"/>
                        </div>
                        <div class="col-right">
                            <xsl:call-template name="meta-block"/>
                        </div>
                    </div>

                    <!-- ===== Titel ===== -->
                    <h1 class="doctitle"><xsl:value-of select="$title"/></h1>

                    <!-- ===== Positionstabelle (nur wenn Positionen vorhanden) ===== -->
                    <xsl:if test="xr:INVOICE_LINE">
                        <xsl:call-template name="position-table"/>
                    </xsl:if>

                    <!-- ===== Summenblock ===== -->
                    <xsl:call-template name="summary-block"/>

                    <!-- ===== Lieferdetails ===== -->
                    <xsl:call-template name="delivery-block"/>

                    <!-- ===== Hinweise und Anmerkungen ===== -->
                    <xsl:call-template name="notes-block"/>

                    <!-- ===== Anlagen ===== -->
                    <xsl:call-template name="attachments-block"/>

                </div>

                <!-- Verstecktes base64-Depot fuer Anhaenge (downloadData-Contract, R6) -->
                <xsl:call-template name="attachment-payloads"/>
            </body>

            <!-- Anhang-Oeffnen-Mechanik (Contract aus xrechnung-html.uni.xsl uebernommen,
                 nicht neu gebaut - AD-006 / R6). HTML-Output-Methode gibt den
                 Script-Inhalt roh aus; Source-CDATA erlaubt &amp;&amp; unescaped. -->
            <script>
<![CDATA[
function base64_to_binary (data) {
  var chars = atob(data);
  var bytes = new Array(chars.length);
  for (var i = 0; i < chars.length; i++) {
    bytes[i] = chars.charCodeAt(i);
  }
  return new Uint8Array(bytes);
}

function downloadData (element_id) {
  var data_element = document.getElementById(element_id);
  if (!data_element) { return; }
  var mimetype = data_element.getAttribute('mimeType');
  var filename = data_element.getAttribute('filename');
  var text = data_element.innerHTML;
  var binary = base64_to_binary(text);
  var blob = new Blob([binary], { type: mimetype, size: binary.length });

  if (window.navigator && window.navigator.msSaveOrOpenBlob) {
    window.navigator.msSaveOrOpenBlob(blob, filename);
  } else {
    var url = window.URL.createObjectURL(blob);
    window.open(url);
  }
}
]]>
            </script>
        </html>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Wiederverwendbare Zeilen-Templates                            -->
    <!-- ============================================================= -->

    <!-- Label-Wert-Zeile in einem Party-/Kopf-Block; entfaellt bei leerem Wert.
         Leeres Label => nur Wert (Name/Adresszeilen). -->
    <xsl:template name="kv">
        <xsl:param name="label" select="''"/>
        <xsl:param name="value" select="''"/>
        <xsl:param name="scheme" select="''"/>
        <xsl:variable name="v" select="normalize-space(string(($value)[1]))"/>
        <xsl:if test="$v != ''">
            <div class="ln">
                <xsl:if test="$label != ''">
                    <span class="lb"><xsl:value-of select="$label"/><xsl:text> </xsl:text></span>
                </xsl:if>
                <span class="vl"><xsl:value-of select="$v"/></span>
                <xsl:if test="normalize-space(string(($scheme)[1])) != ''">
                    <span class="scheme"><xsl:text> (</xsl:text><xsl:value-of select="normalize-space(string(($scheme)[1]))"/><xsl:text>)</xsl:text></span>
                </xsl:if>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- Metadaten-Tabellenzeile (Label grau links, Wert rechts); entfaellt bei leerem Wert -->
    <xsl:template name="meta-row">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <xsl:variable name="v" select="normalize-space(string(($value)[1]))"/>
        <xsl:if test="$v != ''">
            <tr>
                <td class="ml"><xsl:value-of select="$label"/></td>
                <td class="mv"><xsl:value-of select="$v"/></td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- Summenblock-Zeile; entfaellt bei leerem Wert -->
    <xsl:template name="sum-row">
        <xsl:param name="label"/>
        <xsl:param name="value"/>
        <xsl:param name="cls" select="''"/>
        <xsl:variable name="v" select="normalize-space(string(($value)[1]))"/>
        <xsl:if test="$v != ''">
            <tr>
                <xsl:if test="$cls != ''">
                    <xsl:attribute name="class"><xsl:value-of select="$cls"/></xsl:attribute>
                </xsl:if>
                <td class="sl"><xsl:value-of select="$label"/></td>
                <td class="sv"><xsl:value-of select="$v"/></td>
            </tr>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Verkaeufer BG-4                                               -->
    <!-- ============================================================= -->

    <xsl:template name="seller-block">
        <div class="name"><xsl:value-of select="normalize-space((//*[@xr:id='BT-27'])[1])"/></div>
        <!-- Anschrift -->
        <xsl:call-template name="kv">
            <xsl:with-param name="value" select="normalize-space(concat((//*[@xr:id='BT-35'])[1], ' ', (//*[@xr:id='BT-36'])[1]))"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="value" select="normalize-space(concat((//*[@xr:id='BT-38'])[1], ' ', (//*[@xr:id='BT-37'])[1]))"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="value" select="xrv:codetext((//*[@xr:id='BT-40'])[1])"/>
        </xsl:call-template>
        <!-- Kennungen -->
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'ID:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-29']"/>
            <xsl:with-param name="scheme" select="//*[@xr:id='BT-29']/@scheme_identifier"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'USt-ID:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-31']"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'St.-Nr.:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-32']"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'Handelsname:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-28']"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'Registernummer:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-30']"/>
            <xsl:with-param name="scheme" select="//*[@xr:id='BT-30']/@scheme_identifier"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'E-Adresse:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-34']"/>
        </xsl:call-template>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Zahlungsdetails BG-16/17                                      -->
    <!-- ============================================================= -->

    <xsl:template name="payment-block">
        <xsl:if test="//*[@xr:id='BT-85'] | //*[@xr:id='BT-84'] | //*[@xr:id='BT-86'] | //*[@xr:id='BT-81']">
            <div class="blocktitle">Zahlungsdetails</div>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'Kontoinhaber:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-85']"/>
            </xsl:call-template>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'IBAN:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-84']"/>
            </xsl:call-template>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'BIC:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-86']"/>
            </xsl:call-template>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'Zahlungsart:'"/>
                <xsl:with-param name="value" select="xrv:codetext((//*[@xr:id='BT-81'])[1])"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Kontakt Verkaeufer BG-6                                       -->
    <!-- ============================================================= -->

    <xsl:template name="seller-contact-block">
        <xsl:if test="//*[@xr:id='BT-41'] | //*[@xr:id='BT-43'] | //*[@xr:id='BT-42']">
            <div class="blocktitle" style="margin-top:3mm;">Kontakt</div>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'Name:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-41']"/>
            </xsl:call-template>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'E-Mail:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-43']"/>
            </xsl:call-template>
            <xsl:call-template name="kv">
                <xsl:with-param name="label" select="'Tel:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-42']"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Empfaenger BG-7                                               -->
    <!-- ============================================================= -->

    <xsl:template name="buyer-block">
        <div class="name"><xsl:value-of select="normalize-space((//*[@xr:id='BT-44'])[1])"/></div>
        <xsl:call-template name="kv">
            <xsl:with-param name="value" select="normalize-space(concat((//*[@xr:id='BT-50'])[1], ' ', (//*[@xr:id='BT-51'])[1]))"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="value" select="normalize-space(concat((//*[@xr:id='BT-53'])[1], ' ', (//*[@xr:id='BT-52'])[1]))"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="value" select="xrv:codetext((//*[@xr:id='BT-55'])[1])"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'Handelsname:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-45']"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'Registernummer:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-47']"/>
            <xsl:with-param name="scheme" select="//*[@xr:id='BT-47']/@scheme_identifier"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'USt-ID:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-48']"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'Kunden-Nr.:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-46']"/>
            <xsl:with-param name="scheme" select="//*[@xr:id='BT-46']/@scheme_identifier"/>
        </xsl:call-template>
        <xsl:call-template name="kv">
            <xsl:with-param name="label" select="'E-Adresse:'"/>
            <xsl:with-param name="value" select="//*[@xr:id='BT-49']"/>
        </xsl:call-template>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Metadaten-Tabelle (rechts, Zeile 2)                           -->
    <!-- ============================================================= -->

    <xsl:template name="meta-block">
        <!-- BT-13-Platzhalter-Heuristik (R2): gilt als Platzhalter, wenn er mit '#'
             beginnt, keine Ziffer enthaelt oder ueberlanger Freitext ist. -->
        <xsl:variable name="bt13" select="normalize-space((//*[@xr:id='BT-13'])[1])"/>
        <xsl:variable name="bt13placeholder"
                      select="$bt13 != '' and (starts-with($bt13,'#') or not(matches($bt13,'[0-9]')) or string-length($bt13) &gt; 40)"/>
        <!-- Abrechnungszeitraum BT-73..74 -->
        <xsl:variable name="ps" select="xrv:date(//*[@xr:id='BT-73'])"/>
        <xsl:variable name="pe" select="xrv:date(//*[@xr:id='BT-74'])"/>
        <xsl:variable name="period" select="
            if ($ps != '' and $pe != '') then concat($ps, ' - ', $pe)
            else if ($ps != '') then $ps
            else $pe"/>
        <!-- Rechnungsreferenz BT-25 (+ Datum BT-26) -->
        <xsl:variable name="invref" select="normalize-space((//*[@xr:id='BT-25'])[1])"/>
        <xsl:variable name="invrefdate" select="xrv:date(//*[@xr:id='BT-26'])"/>
        <xsl:variable name="invrefstr" select="
            if ($invref != '' and $invrefdate != '') then concat($invref, ' (', $invrefdate, ')')
            else $invref"/>

        <table class="meta">
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Rechnungs-Nr.:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-1']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Rechnungsdatum:'"/>
                <xsl:with-param name="value" select="xrv:date(//*[@xr:id='BT-2'])"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Lieferdatum:'"/>
                <xsl:with-param name="value" select="xrv:date(//*[@xr:id='BT-72'])"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Abrechnungszeitraum:'"/>
                <xsl:with-param name="value" select="$period"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Faelligkeitsdatum:'"/>
                <xsl:with-param name="value" select="xrv:date(//*[@xr:id='BT-9'])"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Kaeuferreferenz:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-10']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Projektnummer:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-11']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Vertragsnummer:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-12']"/>
            </xsl:call-template>
            <!-- Bestell-Nr. BT-13 mit R2-Heuristik -->
            <xsl:choose>
                <xsl:when test="$bt13 != '' and not($bt13placeholder)">
                    <xsl:call-template name="meta-row">
                        <xsl:with-param name="label" select="'Bestell-Nr.:'"/>
                        <xsl:with-param name="value" select="$bt13"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:when test="$bt13placeholder and $hasPO">
                    <xsl:call-template name="meta-row">
                        <xsl:with-param name="label" select="'Bestell-Nr.:'"/>
                        <xsl:with-param name="value" select="'siehe Bestellpos. je Position'"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
            <!-- Auftrags-Nr. BT-14 (R3, klar getrennt von Bestell-Nr.) -->
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Auftrags-Nr.:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-14']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Verwendungszweck:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-83']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Rechnungsreferenz:'"/>
                <xsl:with-param name="value" select="$invrefstr"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Versandanzeige:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-16']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Wareneingangsmeldung:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-15']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Buchhaltungskonto:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-19']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Ausschreibung/Los:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-17']"/>
            </xsl:call-template>
            <xsl:call-template name="meta-row">
                <xsl:with-param name="label" select="'Objektreferenz:'"/>
                <xsl:with-param name="value" select="//*[@xr:id='BT-18']"/>
            </xsl:call-template>
        </table>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Positionstabelle BG-25 (inkl. R1: Bestellpos.-Spalte)         -->
    <!-- ============================================================= -->

    <xsl:template name="position-table">
        <table class="pos">
            <thead>
                <tr>
                    <th class="r">#</th>
                    <xsl:if test="$hasPO"><th>Bestellpos.</th></xsl:if>
                    <th>Position</th>
                    <th class="r">Anzahl</th>
                    <th class="r">Preis</th>
                    <th class="r">Steuer</th>
                    <th class="r">Gesamt</th>
                </tr>
            </thead>
            <tbody>
                <xsl:for-each select="xr:INVOICE_LINE">
                    <xsl:variable name="num"   select="normalize-space((.//*[@xr:id='BT-126'])[1])"/>
                    <xsl:variable name="po"    select="normalize-space((.//*[@xr:id='BT-132'])[1])"/>
                    <xsl:variable name="iname" select="normalize-space((.//*[@xr:id='BT-153'])[1])"/>
                    <xsl:variable name="idesc" select="normalize-space((.//*[@xr:id='BT-154'])[1])"/>
                    <xsl:variable name="qty"   select="(.//*[@xr:id='BT-129'])[1]"/>
                    <xsl:variable name="unit"  select="xrv:codetext((.//*[@xr:id='BT-130'])[1])"/>
                    <xsl:variable name="price" select="(.//*[@xr:id='BT-146'])[1]"/>
                    <xsl:variable name="rate"  select="xrv:codeonly((.//*[@xr:id='BT-152'])[1])"/>
                    <xsl:variable name="cat"   select="xrv:codeonly((.//*[@xr:id='BT-151'])[1])"/>
                    <xsl:variable name="total" select="(.//*[@xr:id='BT-131'])[1]"/>
                    <xsl:variable name="pstart" select="xrv:date((.//*[@xr:id='BT-134'])[1])"/>
                    <xsl:variable name="pend"   select="xrv:date((.//*[@xr:id='BT-135'])[1])"/>
                    <xsl:variable name="sellid" select="normalize-space((.//*[@xr:id='BT-155'])[1])"/>
                    <xsl:variable name="buyid"  select="normalize-space((.//*[@xr:id='BT-156'])[1])"/>
                    <xsl:variable name="cost"   select="normalize-space((.//*[@xr:id='BT-133'])[1])"/>

                    <tr>
                        <td class="idx r"><xsl:value-of select="$num"/></td>

                        <xsl:if test="$hasPO">
                            <td class="po-cell"><xsl:value-of select="$po"/></td>
                        </xsl:if>

                        <td>
                            <div class="itemname"><xsl:value-of select="$iname"/></div>
                            <!-- Degradations-Fallback: wenn keine eigene Spalte, aber BT-132 belegt,
                                 als hervorgehobene Unterzeile (nie ganz verstecken, R1). -->
                            <xsl:if test="not($hasPO) and $po != ''">
                                <div class="subln"><strong>Bestellpos.: <xsl:value-of select="$po"/></strong></div>
                            </xsl:if>
                            <xsl:if test="$idesc != ''">
                                <div class="subln"><xsl:value-of select="$idesc"/></div>
                            </xsl:if>
                            <xsl:if test="$pstart != '' or $pend != ''">
                                <div class="subln">Zeitraum:
                                    <xsl:value-of select="$pstart"/>
                                    <xsl:if test="$pstart != '' and $pend != ''"> - </xsl:if>
                                    <xsl:value-of select="$pend"/>
                                </div>
                            </xsl:if>
                            <xsl:if test="$sellid != ''">
                                <div class="subln">Artikel-Nr.: <xsl:value-of select="$sellid"/></div>
                            </xsl:if>
                            <xsl:if test="$buyid != ''">
                                <div class="subln">Artikel-Nr. (Kaeufer): <xsl:value-of select="$buyid"/></div>
                            </xsl:if>
                            <xsl:if test="$cost != ''">
                                <div class="subln">Kostenstelle: <xsl:value-of select="$cost"/></div>
                            </xsl:if>
                        </td>

                        <td class="r">
                            <xsl:value-of select="xrv:qty($qty)"/>
                            <xsl:if test="$unit != ''"><span class="unit"><xsl:text> </xsl:text><xsl:value-of select="$unit"/></span></xsl:if>
                        </td>
                        <td class="r"><xsl:value-of select="xrv:moneyc($price, $cur)"/></td>
                        <td class="r">
                            <xsl:choose>
                                <xsl:when test="$rate != ''"><xsl:value-of select="xrv:pct($rate)"/><xsl:text> %</xsl:text></xsl:when>
                                <xsl:otherwise><xsl:text>-</xsl:text></xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="$cat != ''"><span class="cat"><xsl:text> </xsl:text><xsl:value-of select="$cat"/></span></xsl:if>
                        </td>
                        <td class="r"><xsl:value-of select="xrv:moneyc($total, $cur)"/></td>
                    </tr>
                </xsl:for-each>
            </tbody>
        </table>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Summenblock BG-22 / BG-23                                     -->
    <!-- ============================================================= -->

    <xsl:template name="summary-block">
        <div class="sums-wrap">
            <table class="sums">
                <xsl:call-template name="sum-row">
                    <xsl:with-param name="label" select="'Summe Positionen (Netto):'"/>
                    <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-106'], $cur)"/>
                </xsl:call-template>
                <xsl:if test="xrv:nz(//*[@xr:id='BT-107'])">
                    <xsl:call-template name="sum-row">
                        <xsl:with-param name="label" select="'Nachlass (Netto):'"/>
                        <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-107'], $cur)"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="xrv:nz(//*[@xr:id='BT-108'])">
                    <xsl:call-template name="sum-row">
                        <xsl:with-param name="label" select="'Zuschlag (Netto):'"/>
                        <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-108'], $cur)"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:call-template name="sum-row">
                    <xsl:with-param name="label" select="'Gesamt (Netto):'"/>
                    <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-109'], $cur)"/>
                    <xsl:with-param name="cls" select="'net-total'"/>
                </xsl:call-template>

                <!-- Steuer je Satz (BG-23): eine Zeile je Steuersatz > 0 -->
                <xsl:for-each select="xr:VAT_BREAKDOWN">
                    <xsl:variable name="vrate" select="xrv:codeonly((.//*[@xr:id='BT-119'])[1])"/>
                    <xsl:variable name="vcat"  select="xrv:codeonly((.//*[@xr:id='BT-118'])[1])"/>
                    <xsl:variable name="vamt"  select="(.//*[@xr:id='BT-117'])[1]"/>
                    <xsl:variable name="vreason" select="normalize-space((.//*[@xr:id='BT-120'])[1])"/>
                    <xsl:choose>
                        <!-- normaler Satz -->
                        <xsl:when test="$vrate != '' and number($vrate) &gt; 0">
                            <xsl:call-template name="sum-row">
                                <xsl:with-param name="label" select="concat('Steuer ', xrv:pct($vrate), ' %', if ($vcat != '') then concat(' (', $vcat, ')') else '', ':')"/>
                                <xsl:with-param name="value" select="xrv:moneyc($vamt, $cur)"/>
                            </xsl:call-template>
                        </xsl:when>
                        <!-- nullsatzig ohne Befreiungsgrund (Kategorie Z): '0 %' zeigen -->
                        <xsl:when test="$vreason = ''">
                            <xsl:call-template name="sum-row">
                                <xsl:with-param name="label" select="concat('Steuer 0 %', if ($vcat != '') then concat(' (', $vcat, ')') else '', ':')"/>
                                <xsl:with-param name="value" select="xrv:moneyc($vamt, $cur)"/>
                            </xsl:call-template>
                        </xsl:when>
                        <!-- befreit mit Grund: keine USt-Summenzeile (Grund als Fussnote unten) -->
                    </xsl:choose>
                </xsl:for-each>

                <xsl:call-template name="sum-row">
                    <xsl:with-param name="label" select="'Gesamt (Brutto):'"/>
                    <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-112'], $cur)"/>
                </xsl:call-template>

                <xsl:if test="xrv:nz(//*[@xr:id='BT-113'])">
                    <xsl:call-template name="sum-row">
                        <xsl:with-param name="label" select="'Gezahlt:'"/>
                        <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-113'], $cur)"/>
                    </xsl:call-template>
                </xsl:if>
                <xsl:if test="xrv:nz(//*[@xr:id='BT-114'])">
                    <xsl:call-template name="sum-row">
                        <xsl:with-param name="label" select="'Rundung:'"/>
                        <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-114'], $cur)"/>
                    </xsl:call-template>
                </xsl:if>

                <xsl:call-template name="sum-row">
                    <xsl:with-param name="label" select="'Faelliger Betrag:'"/>
                    <xsl:with-param name="value" select="xrv:moneyc(//*[@xr:id='BT-115'], $cur)"/>
                    <xsl:with-param name="cls" select="'due'"/>
                </xsl:call-template>
            </table>
        </div>

        <!-- Fremdwaehrung (BT-5 != BT-6): USt-Summe in Buchungswaehrung zusaetzlich (BT-111) -->
        <xsl:if test="$acccur != '' and $acccur != $cur and //*[@xr:id='BT-111']">
            <div class="acc-cur">
                USt gesamt (in <xsl:value-of select="$acccur"/>):
                <xsl:text> </xsl:text>
                <xsl:value-of select="xrv:moneyc(//*[@xr:id='BT-111'], $acccur)"/>
            </div>
        </xsl:if>

        <!-- Steuerbefreiungsgruende (Kategorie E/K/AE/O/G): Grund je betroffener Gruppe -->
        <xsl:for-each select="xr:VAT_BREAKDOWN[normalize-space((.//*[@xr:id='BT-120'])[1]) != '']">
            <xsl:variable name="vcat" select="xrv:codeonly((.//*[@xr:id='BT-118'])[1])"/>
            <div class="vat-reason">
                <xsl:text>Steuerbefreiung</xsl:text>
                <xsl:if test="$vcat != ''"><xsl:text> (</xsl:text><xsl:value-of select="$vcat"/><xsl:text>)</xsl:text></xsl:if>
                <xsl:text>: </xsl:text>
                <xsl:value-of select="normalize-space((.//*[@xr:id='BT-120'])[1])"/>
            </div>
        </xsl:for-each>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Lieferdetails BG-13 (nur falls vorhanden)                     -->
    <!-- ============================================================= -->

    <xsl:template name="delivery-block">
        <xsl:variable name="dname"   select="normalize-space((//*[@xr:id='BT-70'])[1])"/>
        <xsl:variable name="dloc"    select="normalize-space((//*[@xr:id='BT-71'])[1])"/>
        <xsl:variable name="dstreet" select="normalize-space(concat((//*[@xr:id='BT-75'])[1], ' ', (//*[@xr:id='BT-76'])[1]))"/>
        <xsl:variable name="dcity"   select="normalize-space(concat((//*[@xr:id='BT-78'])[1], ' ', (//*[@xr:id='BT-77'])[1]))"/>
        <xsl:variable name="dregion" select="normalize-space((//*[@xr:id='BT-79'])[1])"/>
        <xsl:if test="$dname != '' or $dloc != '' or $dstreet != '' or $dcity != '' or $dregion != ''">
            <div class="section">
                <h2>Lieferdetails</h2>
                <xsl:call-template name="kv">
                    <xsl:with-param name="label" select="'Name:'"/>
                    <xsl:with-param name="value" select="$dname"/>
                </xsl:call-template>
                <xsl:call-template name="kv">
                    <xsl:with-param name="label" select="'Kennung des Ortes:'"/>
                    <xsl:with-param name="value" select="$dloc"/>
                </xsl:call-template>
                <xsl:call-template name="kv">
                    <xsl:with-param name="label" select="'Anschrift:'"/>
                    <xsl:with-param name="value" select="$dstreet"/>
                </xsl:call-template>
                <xsl:call-template name="kv">
                    <xsl:with-param name="value" select="$dcity"/>
                </xsl:call-template>
                <xsl:call-template name="kv">
                    <xsl:with-param name="value" select="$dregion"/>
                </xsl:call-template>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Hinweise und Anmerkungen (BT-20/21/22/33)                     -->
    <!-- ============================================================= -->

    <xsl:template name="notes-block">
        <xsl:variable name="bt20" select="normalize-space((//*[@xr:id='BT-20'])[1])"/>
        <xsl:variable name="bt33" select="normalize-space((//*[@xr:id='BT-33'])[1])"/>
        <xsl:variable name="hasNotes" select="boolean(//*[@xr:id='BT-22'][normalize-space()!=''])"/>
        <xsl:if test="$bt20 != '' or $bt33 != '' or $hasNotes">
            <div class="section">
                <h2>Hinweise und Anmerkungen</h2>
                <xsl:call-template name="kv">
                    <xsl:with-param name="label" select="'Zahlungsbedingungen:'"/>
                    <xsl:with-param name="value" select="$bt20"/>
                </xsl:call-template>
                <xsl:call-template name="kv">
                    <xsl:with-param name="label" select="'Rechtliche Angaben:'"/>
                    <xsl:with-param name="value" select="$bt33"/>
                </xsl:call-template>
                <xsl:for-each select="//*[@xr:id='BT-22'][normalize-space()!='']">
                    <xsl:variable name="raw" select="normalize-space(.)"/>
                    <!-- Betreff-Code (BT-21): CII als Nachbar-Element; UBL als #CODE#-Praefix im Text -->
                    <xsl:variable name="sibcode"
                                  select="normalize-space((preceding-sibling::*[@xr:id='BT-21'][1] | following-sibling::*[@xr:id='BT-21'][1])[1])"/>
                    <xsl:variable name="hasHash" select="$sibcode = '' and matches($raw,'^#[^#]+#')"/>
                    <xsl:variable name="code" select="
                        if ($sibcode != '') then $sibcode
                        else if ($hasHash) then replace($raw,'^#([^#]+)#.*$','$1')
                        else ''"/>
                    <xsl:variable name="text" select="
                        if ($hasHash) then replace($raw,'^#[^#]+#','')
                        else $raw"/>
                    <div class="ln">
                        <xsl:if test="$code != ''"><span class="note-code"><xsl:value-of select="xrv:codeonly($code)"/></span></xsl:if>
                        <span class="vl"><xsl:value-of select="$text"/></span>
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- ============================================================= -->
    <!-- Anlagen BG-24 (R5) - sichtbare Liste                          -->
    <!-- ============================================================= -->

    <xsl:template name="attachments-block">
        <xsl:if test="xr:ADDITIONAL_SUPPORTING_DOCUMENTS">
            <div class="section">
                <h2>Anlagen</h2>
                <xsl:for-each select="xr:ADDITIONAL_SUPPORTING_DOCUMENTS">
                    <xsl:variable name="ref"   select="normalize-space((xr:Supporting_document_reference)[1])"/>
                    <xsl:variable name="desc"  select="normalize-space((xr:Supporting_document_description)[1])"/>
                    <xsl:variable name="uri"   select="normalize-space((xr:External_document_location)[1])"/>
                    <xsl:variable name="fname" select="normalize-space((xr:Attached_document/@filename)[1])"/>
                    <xsl:variable name="mime"  select="normalize-space((xr:Attached_document/@mime_code)[1])"/>
                    <xsl:variable name="hasEmbedded" select="normalize-space((xr:Attached_document)[1]) != ''"/>
                    <!-- eindeutige DOM-Id fuer den downloadData-Contract -->
                    <xsl:variable name="aid" select="if ($ref != '') then $ref else generate-id()"/>
                    <xsl:variable name="displayname" select="
                        if ($fname != '') then $fname
                        else if ($desc != '') then $desc
                        else if ($ref != '') then $ref
                        else 'Anhang'"/>

                    <div class="anlage-row">
                        <span class="vl"><xsl:value-of select="$displayname"/></span>
                        <xsl:if test="$mime != ''">
                            <span class="anlage-mime"><xsl:text> (</xsl:text><xsl:value-of select="$mime"/><xsl:text>)</xsl:text></span>
                        </xsl:if>
                        <xsl:text> </xsl:text>
                        <xsl:choose>
                            <xsl:when test="$hasEmbedded">
                                <a href="#" onClick="downloadData('{$aid}')">Oeffnen</a>
                            </xsl:when>
                            <xsl:when test="$uri != ''">
                                <a href="{$uri}" target="_blank" rel="noopener">Link oeffnen</a>
                            </xsl:when>
                        </xsl:choose>
                        <xsl:if test="$desc != '' and $desc != $displayname">
                            <div class="subln"><xsl:value-of select="$desc"/></div>
                        </xsl:if>
                    </div>
                </xsl:for-each>
            </div>
        </xsl:if>
    </xsl:template>

    <!-- Verstecktes base64-Depot: ein div je eingebettetem Anhang.
         Contract exakt wie xrechnung-html.uni.xsl (id=Referenz, Attribute
         mimetype/filename, base64 als Textinhalt) - damit downloadData() laeuft. -->
    <xsl:template name="attachment-payloads">
        <xsl:for-each select="xr:ADDITIONAL_SUPPORTING_DOCUMENTS[normalize-space((xr:Attached_document)[1]) != '']">
            <xsl:variable name="ref" select="normalize-space((xr:Supporting_document_reference)[1])"/>
            <xsl:variable name="aid" select="if ($ref != '') then $ref else generate-id()"/>
            <div id="{$aid}"
                 mimetype="{xr:Attached_document/@mime_code}"
                 filename="{xr:Attached_document/@filename}"
                 style="display:none;"><xsl:value-of select="xr:Attached_document"/></div>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
