package lechuck.kakaopay.dialect

/**
 * 수정된 H2 Dialect
 *
 * - H2 JDBC 드라이버에서 DATABASE_TO_UPPER=false 를 설정시 information_schema.sequences 를 찾지 못하는 문제 수정
 */
class H2Dialect : org.hibernate.dialect.H2Dialect() {
    override fun getQuerySequencesString(): String {
        return "select * from INFORMATION_SCHEMA.SEQUENCES"
    }
}
