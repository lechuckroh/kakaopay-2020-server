.PHONY: ojson xlsx

BASENAME=database
UNIQUE_SUFFIX=_uq

ojson:
	./oct convert $(BASENAME).xlsx $(BASENAME).ojson

xlsx:
	./oct convert $(BASENAME).ojson $(BASENAME).xlsx

liquibase:
	./oct generate $(BASENAME).ojson ./output \
		--targetFormat=liquibase \
		--uniqueNameSuffix=$(UNIQUE_SUFFIX)

jpa-kotlin:
	./oct generate $(BASENAME).ojson ./output \
		--targetFormat=jpa-kotlin-data \
		--package=lechuck.kakaopay.entity \
		--reposPackage=lechuck.kakaopay.repos \
		--uniqueNameSuffix=$(UNIQUE_SUFFIX)
