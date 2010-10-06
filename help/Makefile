# Add your language file here:
TARGETS = main.html

XSLTPROC = /usr/bin/xsltproc
;XSL = /usr/share/xml/docbook/stylesheet/nwalsh/xhtml/docbook.xsl
XSL = /usr/share/xml/docbook/stylesheet/docbook-xsl/html/docbook.xsl 


%.html: %.xml $(XSL)
	@$(XSLTPROC) -o $@ $(XSL) $<

all: $(TARGETS)

clean:
	@rm -f *.html
