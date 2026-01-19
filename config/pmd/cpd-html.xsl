<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="yes" />

  <xsl:template match="/">
    <html>
      <head>
        <title>CPD Report</title>
        <style type="text/css">
          body { font-family: Arial, sans-serif; margin: 20px; }
          h1 { margin-bottom: 10px; }
          pre { background: #f5f5f5; padding: 10px; border: 1px solid #ddd; overflow: auto; }
          .meta { color: #555; }
        </style>
      </head>
      <body>
        <h1>CPD Report</h1>
        <xsl:choose>
          <xsl:when test="count(/pmd-cpd/duplication) = 0">
            <p>No duplicates found.</p>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="/pmd-cpd/duplication">
              <h2>Duplication <xsl:value-of select="position()"/></h2>
              <p class="meta">Lines: <xsl:value-of select="@lines"/> / Tokens: <xsl:value-of select="@tokens"/></p>
              <ul>
                <xsl:for-each select="file">
                  <li>
                    <xsl:value-of select="@path"/> (line <xsl:value-of select="@line"/>)
                  </li>
                </xsl:for-each>
              </ul>
              <pre><xsl:value-of select="codefragment"/></pre>
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </body>
    </html>
  </xsl:template>
</xsl:stylesheet>
