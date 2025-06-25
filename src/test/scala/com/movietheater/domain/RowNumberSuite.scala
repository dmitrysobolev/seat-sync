
package com.movietheater.domain

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class RowNumberSuite extends AnyFunSuite with Matchers {

  test("fromString should correctly parse a valid row number") {
    RowNumber.fromString("A") should be(Some(RowNumber('A')))
  }

  test("fromString should convert a lowercase row number to uppercase") {
    RowNumber.fromString("a") should be(Some(RowNumber('A')))
  }

  test("fromString should return None for a multi-character string") {
    RowNumber.fromString("AB") should be(None)
  }

  test("fromString should return None for a non-letter string") {
    RowNumber.fromString("1") should be(None)
  }

  test("toString should return the correct character") {
    RowNumber('A').toString should be("A")
  }
}
