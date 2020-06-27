FROM openjdk:14 as builder

COPY . /tmp/app
WORKDIR /tmp/app

RUN ./gradlew clean build
WORKDIR /tmp/app/build/distributions
RUN tar xvf kakaopay.tar


FROM openjdk:14
LABEL MAINTAINER="Lechuck Roh<lechuckroh@gmail.com>"

COPY --from=builder /tmp/app/build/distributions/kakaopay /usr/src/kakaopay
ADD tools/wait /
RUN chmod +x /wait

WORKDIR /usr/src/kakaopay/bin

EXPOSE 8080

CMD /wait && ./kakaopay
