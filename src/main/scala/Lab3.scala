object Lab3 extends jsy.util.JsyApplication {
  import jsy.lab3.ast._

  /*
	 * CSCI 3155: Lab 3 
	 * Catherine Youngblood
	 * 
	 * Partner: Tristan Hill, Abeve Tayachow
	 */

  /*
	 *  Rename the object above to Lab3-<your identikey> and rename the file before turning in.
	 * 
	 */

  type Env = Map[String, Expr]
  val emp: Env = Map()
  def get(env: Env, x: String): Expr = env(x)
  def extend(env: Env, x: String, v: Expr): Env = {
    require(isValue(v))
    env + (x -> v)
  }

  /*-----HELPER FUNCTIONS-------------------------------------------------------*/
  def toNumber(v: Expr): Double = {
    require(isValue(v))
    (v: @unchecked) match {
      case N(n) => n
      case B(false) => 0
      case B(true) => 1
      case Undefined => Double.NaN
      case S(s) => try s.toDouble catch { case _: Throwable => Double.NaN }
      case Function(_, _, _) => Double.NaN
    }
  }

  def toBoolean(v: Expr): Boolean = {
    require(isValue(v))
    (v: @unchecked) match {
      case N(n) if (n compare 0.0) == 0 || (n compare -0.0) == 0 || n.isNaN => false
      case N(_) => true
      case B(b) => b
      case Undefined => false
      case S("") => false
      case S(_) => true
      case Function(_, _, _) => true
    }
  }

  def toStr(v: Expr): String = {
    require(isValue(v))
    (v: @unchecked) match {
      case N(n) => if (n.isWhole) "%.0f" format n else n.toString
      case B(b) => b.toString
      case Undefined => "undefined"
      case S(s) => s
      case Function(_, _, _) => "function"
    }
  }

  /*----------------------------------------------------------------------------*/

  /*
	 * Helper function that implements the semantics of inequality
	 * operators Lt, Le, Gt, and Ge on values.
	 */
  def inequalityVal(bop: Bop, v1: Expr, v2: Expr): Boolean = {
    require(isValue(v1))
    require(isValue(v2))
    require(bop == Lt || bop == Le || bop == Gt || bop == Ge)
    (v1, v2) match {
      case (S(s1), S(s2)) =>
        (bop: @unchecked) match {
          case Lt => s1 < s2
          case Le => s1 <= s2
          case Gt => s1 > s2
          case Ge => s1 >= s2
        }
      case _ =>
        val (n1, n2) = (toNumber(v1), toNumber(v2))
        (bop: @unchecked) match {
          case Lt => n1 < n2
          case Le => n1 <= n2
          case Gt => n1 > n2
          case Ge => n1 >= n2
        }
    }
  }

  def equalityVal(bop: Bop, v1: Expr, v2: Expr): Boolean = {
    require(isValue(v1))
    require(isValue(v2))
    require(bop == Eq || bop == Ne)
    (v1, v2) match {
      case (S(s1), S(s2)) =>
        (bop: @unchecked) match {
          case Eq => s1 == s2
          case Ne => s1 != s2
        }
      case _ =>
        val (n1, n2) = (toNumber(v1), toNumber(v2))
        (bop: @unchecked) match {
          case Eq => n1 == n2
          case Ne => n1 != n2
        }
    }
  }

  /* Big-Step Interpreter with Dynamic Scoping */

  /*
	 * This code is a reference implementation of JavaScripty without
	 * strings and functions (i.e., Lab 2).  You are to welcome to
	 * replace it with your code from Lab 2.
	 */

  def eval(env: Env, e: Expr): Expr = {
    def eToN(e: Expr): Double = toNumber(eval(env, e))
    def eToB(e: Expr): Boolean = toBoolean(eval(env, e))
    def eToVal(e: Expr): Expr = eval(env, e)
    e match {
      /*----Base Cases----*/
      case _ if isValue(e) => e
      case Var(x) => get(env, x)

      /*----Inductive Cases----*/
      case Print(e1) =>
        println(pretty(eval(env, e1))); Undefined

      //------------
      case Unary(Neg, e1) => N(-eToN(e1))
      case Unary(Not, e1) => B(!eToB(e1))

      //------------
      case Binary(Plus, e1, e2) => (eToVal(e1), eToVal(e2)) match {
        case (S(s1), v2) => S(s1 + toStr(v2))
        case (v1, S(s2)) => S(toStr(v1) + s2)
        case (v1, v2) => N(toNumber(v1) + toNumber(v2))
      }
      case Binary(Minus, e1, e2) => N(eToN(e1) - eToN(e2))
      case Binary(Times, e1, e2) => N(eToN(e1) * eToN(e2))
      case Binary(Div, e1, e2) => N(eToN(e1) / eToN(e2))
      case Binary(bop @ (Eq | Ne), e1, e2) => (e1, e2) match {
        case (Function(_, x1, e1), _) => throw DynamicTypeError(e1)
        case (_, Function(_, x2, e2)) => throw DynamicTypeError(e2)
        case _ => B(equalityVal(bop, eToVal(e1), eToVal(e2)))
      }
      case Binary(bop @ (Lt | Le | Gt | Ge), e1, e2) => B(inequalityVal(bop, eToVal(e1), eToVal(e2)))
      case Binary(And, e1, e2) =>
        val v1 = eToVal(e1)
        if (toBoolean(v1)) eToVal(e2) else v1
      case Binary(Or, e1, e2) =>
        val v1 = eToVal(e1)
        if (toBoolean(v1)) v1 else eToVal(e2)
      case Binary(Seq, e1, e2) =>
        eToVal(e1); eToVal(e2)

      //------------
      case If(e1, e2, e3) => if (eToB(e1)) eToVal(e2) else eToVal(e3)

      //------------
      case ConstDecl(x, e1, e2) => eval(extend(env, x, eToVal(e1)), e2)

      //------------
      case Call(e1, e2) => eToVal(e1) match {
        case Function(None, x, e) => eval(extend(env, x, eToVal(e2)), e)
        case Function(Some(f), x, e) => eval(extend(extend(env, x, eToVal(e2)), f, eToVal(e1)), e)
        case _ => throw new UnsupportedOperationException
      }

      //------------
      case _ => throw new IllegalArgumentException
    }
  }

  /* Small-Step Interpreter with Static Scoping */

  def substitute(e: Expr, v: Expr, x: String): Expr = {
    require(isValue(v))
    /* Simple helper that calls substitute on an expression
		 * with the input value v and variable name x. */
    def subst(e: Expr): Expr = substitute(e, v, x)
    /* Body */
    e match {
      case N(_) | B(_) | Undefined | S(_) => e
      case Print(e1) => Print(subst(e1))
      case _ => throw new UnsupportedOperationException
    }
  }

  def step(e: Expr): Expr = {
    e match {
      /* Base Cases: Do Rules */
      /*--UNARY-----*/
      case Unary(Neg, e1) if isValue(e1) => N(-toNumber(e1))
      case Unary(Not, e1) if isValue(e1) => B(!toBoolean(e1))
      /*--BINARY----*/
      case Binary(Seq, v1, e2) if isValue(e2) => e2
      case Binary(Plus, e1, e2) if (isValue(e1) && isValue(e2)) => (e1, e2) match {
        case (S(s), v) => S(s + toStr(v))
        case (v, S(s)) => S(s + toStr(v))
        case (v1, v2) => N(toNumber(v1) + toNumber(v2))
      }
      case Binary(Minus, e1, e2) if (isValue(e1) && isValue(e2)) => N(toNumber(e1) - toNumber(e2))
      case Binary(Times, e1, e2) if (isValue(e1) && isValue(e2)) => N(toNumber(e1) * toNumber(e2))
      case Binary(Div, e1, e2) if (isValue(e1) && isValue(e2)) => N(toNumber(e1) / toNumber(e2))
      case Binary(bop @ (Lt | Le | Gt | Ge), e1, e2) if (isValue(e1) && isValue(e2)) => B(inequalityVal(bop, e1, e2))
      case Binary(bop @ (Eq | Ne), e1, e2) if (isValue(e1) && isValue(e2)) => (e1, e2) match {
        case (Function(_, x1, e1), _) => throw DynamicTypeError(e1)
        case (_, Function(_, x2, e2)) => throw DynamicTypeError(e2)
        case _ => B(equalityVal(bop, e1, e2))
      }
      case Binary(And, e1, e2) if (isValue(e1) && isValue(e2)) => { val v1 = e1; if (toBoolean(v1)) { e2 } else { v1 } }
      case Binary(Or, e1, e2) if (isValue(e1) && isValue(e2)) => { val v1 = e1; if (toBoolean(v1)) { v1 } else { e2 } }
      /*--OTHER-----*/
      case Print(v1) if isValue(v1) => { println(pretty(v1)); Undefined }
      case If(v1, e2, e3) if (isValue(e2) && isValue(e3)) => if (toBoolean(v1)) { e2 } else { e3 }

      case ConstDecl(x, v1, e2) if isValue(v1) => substitute(e2, v1, x)
      case Call(v1, v2) if (isValue(v1) && isValue(v2)) => v1 match {
        case Function(None, x, e1) => substitute(e1, v2, x)
        case Function(Some(x1), x2, e1) => /**/ throw new UnsupportedOperationException
        case _ => throw new UnsupportedOperationException
      }

      /* Inductive Cases: Search Rules */
      /*------UNARY---------*/
      case Unary(Neg, e1) => N(-toNumber(step(e1)))
      case Unary(Not, e1) => B(!toBoolean(step(e1)))

      /*------BINARY--------*/

      /*---SearchBinaryArith2 (fig 8)---*/
      case Binary(Plus, e1, e2) if isValue(e1) => (e1, e2) match {
        /*If 1st (e1) is value, step on second (e2)*/
        case (S(s1), e2) => S(s1 + toStr(step(e2)))
        case (v1, S(s2)) => S(s2 + toStr(v1))
        case (v1, e2) => N(toNumber(v1) + toNumber(step(e2)))
      }
      case Binary(Minus, e1, e2) if isValue(e1) => N(toNumber(e1) - toNumber(step(e2)))
      case Binary(Times, e1, e2) if isValue(e1) => N(toNumber(e1) * toNumber(step(e2)))
      case Binary(Div, e1, e2) if isValue(e1) => N(toNumber(e1) / toNumber(step(e2)))
      case Binary(bop @ (Lt | Le | Gt | Ge), e1, e2) if isValue(e1) => B(inequalityVal(bop, e1, step(e2)))

      /*---SearchBinary1 (fig 8)---*/
      /*Assumes that the second operator is a value? no e2 -> e2'*/
      case Binary(Plus, e1, e2) => (e1, e2) match {
        /*step on 1st (e1) - at this point we know it's not a value*/
        case (e1, S(s2)) => S(s2 + toStr(step(e1)))
        case (e1, e2) => N(toNumber(step(e1)) + toNumber(e2)) /*doesn't tell us to evaluate v2... (could be a problem if it isn't a value) */
      }
      case Binary(Minus, e1, e2) => N(toNumber(step(e1)) - toNumber(e2))
      case Binary(Times, e1, e2) => N(toNumber(step(e1)) * toNumber(e2))
      case Binary(Div, e1, e2) => N(toNumber(step(e1)) / toNumber(e2))
      case Binary(bop @ (Lt | Le | Gt | Ge), e1, e2) => B(inequalityVal(bop, step(e1), e2))
      case Binary(And, e1, e2) => { val v1 = step(e1); if (toBoolean(v1)) { e2 } else { v1 } }
      case Binary(Or, e1, e2) => { val v1 = step(e1); if (toBoolean(v1)) { v1 } else { e2 } }

      /*---SearchEquality2---*/
      case Binary(bop @ (Eq | Ne), e1, e2) if isValue(e1) => (e1, e2) match {
        case (Function(_, x1, e1), _) => throw DynamicTypeError(e1)
        case (_, Function(_, x2, e2)) => throw DynamicTypeError(e2)
        case _ => B(equalityVal(bop, e1, step(e2)))
      }
      //added this one in because there is no SearchEquality1
      case Binary(bop @ (Eq | Ne), e1, e2) => (e1, e2) match {
        case (Function(_, x1, e1), _) => throw DynamicTypeError(e1)
        case (_, Function(_, x2, e2)) => throw DynamicTypeError(e2)
        case _ => B(equalityVal(bop, step(e1), e2))
      }
      /*---SearchPrint---*/
      case Print(e1) => { println(pretty(step(e1))); Undefined }
      /*---SearchIf---*/
      case If(e1, e2, e3) => if (toBoolean(step(e1))) { e2 } else { e3 }
      /*---SearchConst---*/
      case ConstDecl(x, e1, e2) => { substitute(e2, step(e1), x) }
      /*---SearchCall1 && SearchCall2---*/
      case Call(e1, e2) => e1 match {
        case Function(None, x, e) => /*substitute()*/ throw new UnsupportedOperationException
        case Function(Some(f), x, e) => /**/ throw new UnsupportedOperationException
        case _ => throw new UnsupportedOperationException
      }

      /* Cases that should never match. Your cases above should ensure this. */
      case Var(_) => throw new AssertionError("Gremlins: internal error, not closed expression.")
      case N(_) | B(_) | Undefined | S(_) | Function(_, _, _) => throw new AssertionError("Gremlins: internal error, step should not be called on values.");
    }
  }

  /* External Interfaces */

  this.debug = true // comment this out or set to false if you don't want print debugging information

  // Interface to run your big-step interpreter starting from an empty environment and print out
  // the test input if debugging.
  def evaluate(e: Expr): Expr = {
    require(closed(e))
    if (debug) {
      println("------------------------------------------------------------")
      println("Evaluating with eval ...")
      println("\nExpression:\n  " + e)
    }
    val v = eval(emp, e)
    if (debug) {
      println("Value: " + v)
    }
    v
  }

  // Convenience to pass in a jsy expression as a string.
  def evaluate(s: String): Expr = evaluate(jsy.lab3.Parser.parse(s))

  // Interface to run your small-step interpreter and print out the steps of evaluation if debugging. 
  def iterateStep(e: Expr): Expr = {
    require(closed(e))
    def loop(e: Expr, n: Int): Expr = {
      if (debug) { println("Step %s: %s".format(n, e)) }
      if (isValue(e)) e else loop(step(e), n + 1)
    }
    if (debug) {
      println("------------------------------------------------------------")
      println("Evaluating with step ...")
    }
    val v = loop(e, 0)
    if (debug) {
      println("Value: " + v)
    }
    v
  }

  // Convenience to pass in a jsy expression as a string.
  def iterateStep(s: String): Expr = iterateStep(jsy.lab3.Parser.parse(s))

  // Interface for main
  def processFile(file: java.io.File) {
    if (debug) {
      println("============================================================")
      println("File: " + file.getName)
      println("Parsing ...")
    }

    val expr =
      handle(None: Option[Expr]) {
        Some {
          jsy.lab3.Parser.parseFile(file)
        }
      } getOrElse {
        return
      }

    handle() {
      val v = evaluate(expr)
      println(pretty(v))
    }

    handle() {
      val v1 = iterateStep(expr)
      println(pretty(v1))
    }
  }

}
