FROM openjdk:14 as build

COPY . /tmp/app
WORKDIR /tmp/app

RUN ./gradlew clean build


FROM openjdk:14 as release
LABEL MAINTAINER="Lechuck Roh<lechuckroh@gmail.com>"

RUN mkdir -p /app
COPY --from=build /tmp/app/build/distributions/kakaopay.tar /
ADD tools/wait /wait
RUN chmod +x /wait
WORKDIR /kakaopay/bin

EXPOSE 8080

CMD /wait && ./kakaopay
