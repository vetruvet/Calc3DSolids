import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

public class Equation {
	private String equString, indepVar;
	private Operation rootOperation;
	private boolean isParsed = false;
	private Operand[] eqOpList;
	private BigDecimal[] eqConstList;
		
	public Equation(String equ) {
		this(equ, "x");
	}
	
	public Equation(String equ, String indVar) {
		equString = equ.toLowerCase();
		indepVar = indVar.toLowerCase();
	}
	
	public void parseFunc() throws FunctionParseException {
		if (isParsed) return;
		if (!isEqValid()) throw new FunctionParseException("Error while parsing function: check the syntax");
		
		ArrayList<Operand> opList = new ArrayList<Operand>();
		ArrayList<BigDecimal> consts = new ArrayList<BigDecimal>();
		for (int q = 0; q < equString.length(); ) {
			if (equString.startsWith(indepVar, q)) { 
				opList.add(Operand.INDEP_VAR); 	q += 1; }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.SIN), q)) {
				opList.add(Operand.SIN); 			q += CalcUtils.operandStrLen(Operand.SIN); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.COS), q)) {
				opList.add(Operand.COS); 			q += CalcUtils.operandStrLen(Operand.COS); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.TAN), q)) {
				opList.add(Operand.TAN); 			q += CalcUtils.operandStrLen(Operand.TAN); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.CSC), q)) {
				opList.add(Operand.CSC); 			q += CalcUtils.operandStrLen(Operand.CSC); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.SEC), q)) {
				opList.add(Operand.SEC); 			q += CalcUtils.operandStrLen(Operand.SEC); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.COT), q)) {
				opList.add(Operand.COT); 			q += CalcUtils.operandStrLen(Operand.COT); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ARCSIN), q)) {
				opList.add(Operand.ARCSIN); 		q += CalcUtils.operandStrLen(Operand.ARCSIN); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ARCCOS), q)) {
				opList.add(Operand.ARCCOS); 		q += CalcUtils.operandStrLen(Operand.ARCCOS); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ARCTAN), q)) {
				opList.add(Operand.ARCTAN); 		q += CalcUtils.operandStrLen(Operand.ARCTAN); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ARCCSC), q)) {
				opList.add(Operand.ARCCSC); 		q += CalcUtils.operandStrLen(Operand.ARCCSC); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ARCSEC), q)) {
				opList.add(Operand.ARCSEC); 		q += CalcUtils.operandStrLen(Operand.ARCSEC); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ARCCOT), q)) {
				opList.add(Operand.ARCCOT); 		q += CalcUtils.operandStrLen(Operand.ARCCOT); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.NLOG), q)) {
				opList.add(Operand.NLOG); 			q += CalcUtils.operandStrLen(Operand.NLOG); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.LOG), q)) {
				opList.add(Operand.LOG); 			q += CalcUtils.operandStrLen(Operand.LOG); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.SQRT), q)) {
				opList.add(Operand.SQRT); 			q += CalcUtils.operandStrLen(Operand.SQRT); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.CBRT), q)) {
				opList.add(Operand.CBRT);			q += CalcUtils.operandStrLen(Operand.CBRT); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.ABS), q)) {
				opList.add(Operand.ABS);			q += CalcUtils.operandStrLen(Operand.ABS); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.POW), q)) {
				opList.add(Operand.POW); 			q += CalcUtils.operandStrLen(Operand.POW); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.DIV), q)) {
				opList.add(Operand.DIV); 			q += CalcUtils.operandStrLen(Operand.DIV); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.MULT), q)) {
				opList.add(Operand.MULT); 			q += CalcUtils.operandStrLen(Operand.MULT); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.PLUS), q)) {
				opList.add(Operand.PLUS); 			q += CalcUtils.operandStrLen(Operand.PLUS); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.MINUS), q)) {
				opList.add(Operand.MINUS);			q += CalcUtils.operandStrLen(Operand.MINUS); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.OP), q)) {
				opList.add(Operand.OP); 			q += CalcUtils.operandStrLen(Operand.OP); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.CP), q)) {
				opList.add(Operand.CP); 			q += CalcUtils.operandStrLen(Operand.CP); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.CONST_E), q)) {
				opList.add(Operand.CONST_E); 		q += CalcUtils.operandStrLen(Operand.CONST_E); }
			else if (equString.startsWith(CalcUtils.operandAsString(Operand.CONST_PI), q)) {
				opList.add(Operand.CONST_PI);		q += CalcUtils.operandStrLen(Operand.CONST_PI); }
			else {
				opList.add(Operand.CONSTANT);
				BigDecimal cst = BigDecimal.ZERO;
				int c = 0;
				boolean loopGo = true;
				while (loopGo) {
					try {
						cst = new BigDecimal(equString.substring(q, q + c + 1));
						c++;
						if (q + c == equString.length()) loopGo = false;
					}
					catch (NumberFormatException eNF) {
						loopGo = false;
					}
				}
				consts.add(cst);
				q += c;
			}
			if (opList.size() < 2) continue;
			int prevInd = opList.size() - 1;
			if ((opList.get(prevInd - 1) == Operand.CONSTANT) || (opList.get(prevInd - 1) == Operand.CONST_E) || (opList.get(prevInd - 1) == Operand.CONST_PI)) {
				if ((opList.get(prevInd) == Operand.INDEP_VAR) 	||
						(opList.get(prevInd) == Operand.SIN) 	||
						(opList.get(prevInd) == Operand.COS) 	||
						(opList.get(prevInd) == Operand.TAN) 	||
						(opList.get(prevInd) == Operand.SEC) 	||
						(opList.get(prevInd) == Operand.CSC) 	||
						(opList.get(prevInd) == Operand.COT) 	||
						(opList.get(prevInd) == Operand.ARCSIN)	||
						(opList.get(prevInd) == Operand.ARCCOS)	||
						(opList.get(prevInd) == Operand.ARCTAN)	||
						(opList.get(prevInd) == Operand.ARCSEC)	||
						(opList.get(prevInd) == Operand.ARCCSC)	||
						(opList.get(prevInd) == Operand.ARCCOT)	||
						(opList.get(prevInd) == Operand.LOG) 	||
						(opList.get(prevInd) == Operand.NLOG) 	||
						(opList.get(prevInd) == Operand.OP) 	||
						(opList.get(prevInd) == Operand.ABS)	||
						(opList.get(prevInd) == Operand.CBRT)	||
						(opList.get(prevInd) == Operand.SQRT)) opList.add(prevInd, Operand.MULT);
			}
		}
		
		eqOpList = new Operand[opList.size()];
		eqConstList = new BigDecimal[opList.size()];
		
		Iterator<BigDecimal> itD = consts.iterator();
		int q = 0;
		for (Iterator<Operand> itI = opList.iterator(); itI.hasNext(); q++) {
			eqOpList[q] = itI.next();
			if (eqOpList[q] == Operand.CONSTANT) eqConstList[q] = itD.next();
			else if (eqOpList[q] == Operand.CONST_PI) eqConstList[q] = CalcConst.PI;
			else if (eqOpList[q] == Operand.CONST_E) eqConstList[q] = CalcConst.E;
			else eqConstList[q] = BigDecimal.ZERO;
		}
		
		rootOperation = parseExpr(0, eqOpList.length - 1);
		
		isParsed = true;
	}
	
	private Operation parseExpr(int lowInd, int upInd) {
		if (lowInd == upInd) {
			if (eqOpList[lowInd] == Operand.INDEP_VAR) return new Operation(Operand.INDEP_VAR, BigDecimal.ZERO);
			if (eqOpList[lowInd] == Operand.CONSTANT) return new Operation(Operand.CONSTANT, eqConstList[lowInd]);
			if (eqOpList[lowInd] == Operand.CONST_E) return new Operation(Operand.CONSTANT, CalcConst.E);
			if (eqOpList[lowInd] == Operand.CONST_PI) return new Operation(Operand.CONSTANT, CalcConst.PI);
		}
		
		Operation retOp = new Operation();
		boolean found2Op, found1Op;
		found2Op = found1Op =false;
		int nOp = 0;
		int foundAt = upInd;
		
		for (int q = upInd; q >= lowInd; q--) {
			if (eqOpList[q] == Operand.OP) nOp++;
			if (eqOpList[q] == Operand.CP) nOp--;
			if (nOp != 0) continue;
			if (eqOpList[q] == Operand.PLUS) {
				retOp.setOpCode(Operand.PLUS);
				found2Op = true;
				foundAt = q;
				break;
			}
			else if (eqOpList[q] == Operand.MINUS) {
				retOp.setOpCode(Operand.MINUS);
				found2Op = true;
				foundAt = q;
				break;
			}
		}
		nOp = 0;
		for (int q = upInd; q >= lowInd; q--) {
			if (found2Op) break;
			if (eqOpList[q] == Operand.OP) nOp++;
			if (eqOpList[q] == Operand.CP) nOp--;
			if (nOp != 0) continue;
			if (eqOpList[q] == Operand.MULT) {
				retOp.setOpCode(Operand.MULT);
				found2Op = true;
				foundAt = q;
				break;
			}
			else if (eqOpList[q] == Operand.DIV) {
				retOp.setOpCode(Operand.DIV);
				found2Op = true;
				foundAt = q;
				break;
			}
		}
		nOp = 0;
		for (int q = upInd; q >= lowInd; q--) {
			if (found2Op) break;
			if (eqOpList[q] == Operand.OP) nOp++;
			if (eqOpList[q] == Operand.CP) nOp--;
			if (nOp != 0) continue;
			if (eqOpList[q] == Operand.POW) {
				retOp.setOpCode(Operand.POW);
				found2Op = true;
				foundAt = q;
				break;
			}
		}
		nOp = 0;
		for (int q = upInd; q >= lowInd; q--) {
			if (found2Op) break;
			if (eqOpList[q] == Operand.OP) nOp++;
			if (eqOpList[q] == Operand.CP) nOp--;
			if (nOp != 0) continue;
			switch (eqOpList[q]) {
			case SQRT:
				retOp.setOpCode(Operand.SQRT); 		found1Op = true; break;
			case CBRT:
				retOp.setOpCode(Operand.CBRT); 		found1Op = true; break;
			case LOG:
				retOp.setOpCode(Operand.LOG); 		found1Op = true; break;
			case NLOG:
				retOp.setOpCode(Operand.NLOG); 		found1Op = true; break;
			case SIN:
				retOp.setOpCode(Operand.SIN); 		found1Op = true; break;
			case COS:
				retOp.setOpCode(Operand.COS); 		found1Op = true; break;
			case TAN:
				retOp.setOpCode(Operand.TAN); 		found1Op = true; break;
			case CSC:
				retOp.setOpCode(Operand.CSC);		found1Op = true; break;
			case SEC:
				retOp.setOpCode(Operand.SEC); 		found1Op = true; break;
			case COT:
				retOp.setOpCode(Operand.COT); 		found1Op = true; break;
			case ARCSIN:
				retOp.setOpCode(Operand.ARCSIN); 	found1Op = true; break;
			case ARCCOS:
				retOp.setOpCode(Operand.ARCCOS); 	found1Op = true; break;
			case ARCTAN:
				retOp.setOpCode(Operand.ARCTAN); 	found1Op = true; break;
			case ARCCSC:
				retOp.setOpCode(Operand.ARCCSC); 	found1Op = true; break;
			case ARCSEC:
				retOp.setOpCode(Operand.ARCSEC); 	found1Op = true; break;
			case ARCCOT:
				retOp.setOpCode(Operand.ARCCOT); 	found1Op = true; break;
			case NEG:
				retOp.setOpCode(Operand.NEG); 		found1Op = true; break;
			case ABS:
				retOp.setOpCode(Operand.ABS); 		found1Op = true; break;
			default:
				
			}
			if (found1Op) {
				foundAt = q;
				break;
			}
		}
		
		if (found2Op) {
			if ((foundAt == lowInd) && (retOp.getOpCode() == Operand.MINUS)) {
				retOp.setOpCode(Operand.NEG);
				found1Op = true;
				found2Op = false;
			}
			else {
				if ((eqOpList[foundAt - 1] == Operand.CP) && (eqOpList[lowInd] == Operand.OP)) retOp.setOperand1(parseExpr(lowInd + 1, foundAt - 2));
				else retOp.setOperand1(parseExpr(lowInd, foundAt - 1));
				if ((eqOpList[foundAt + 1] == Operand.OP) && (eqOpList[upInd] == Operand.CP)) retOp.setOperand2(parseExpr(foundAt + 2, upInd - 1));
				else retOp.setOperand2(parseExpr(foundAt + 1, upInd));
			}
		}
		if (found1Op) {
			if ((eqOpList[foundAt + 1] == Operand.OP) && (eqOpList[upInd] == Operand.CP)) retOp.setOperand1(parseExpr(foundAt + 2, upInd - 1));
			else retOp.setOperand1(parseExpr(foundAt + 1, upInd));
		}
		
		return retOp;
	}
	
	public BigDecimal eval(BigDecimal indVar) throws OperatorDomainException {
		if (!isParsed) return null;
		try {
			return rootOperation.eval(indVar.round(CalcConst.MC)).round(CalcConst.MC);
		}
		catch (InvalidOperatorException eIO) {
			return null;
		}
	}
	
	public boolean isEqValid() {
		int nOpenP = CalcUtils.matchCount(equString, "(");
		int nClseP = CalcUtils.matchCount(equString, ")");
		if (nOpenP != nClseP) return false;
		if (equString.indexOf("()") != -1) return false;
		
		char fChar = equString.charAt(0);
		char lChar = equString.charAt(equString.length() - 1);
		if ((fChar == '+') || (fChar == '*') || (fChar == '/') || (fChar == '^') || (fChar == ')')) return false;
		if ((lChar == '+') || (lChar == '-') || (lChar == '*') || (lChar == '/') || (lChar == '^') || (lChar == '(')) return false;
		
		if (isEqInX() && (equString.indexOf('y') != -1)) return false;
		if (!isEqInX() && (equString.indexOf('x') != -1)) return false;
		
		//characters not used in any operations.
		if (equString.indexOf('f') != -1) return false;
		if (equString.indexOf('h') != -1) return false;
		if (equString.indexOf('j') != -1) return false;
		if (equString.indexOf('k') != -1) return false;
		if (equString.indexOf('m') != -1) return false;
		if (equString.indexOf('u') != -1) return false;
		if (equString.indexOf('v') != -1) return false;
		if (equString.indexOf('w') != -1) return false;
		if (equString.indexOf('z') != -1) return false;
		
		return true;
	}
	
	public boolean contains(Point2DBig pt) {
		if (isEqInX()) {
			try {
				return pt.getY().compareTo(eval(pt.getX())) == 0;
			} 
			catch (OperatorDomainException e) {
				return false;
			}
		}
		else {
			try {
				return pt.getX().compareTo(eval(pt.getX())) == 0;
			} 
			catch (OperatorDomainException e) {
				return false;
			}
		}
	}
	
	public Point2DBig[] intersectsWith(Equation equ) {
		if (!this.isEqParsed() || !equ.isEqParsed()) return null;
		return intersectsWith(equ, new BigDecimal("-10.0"), new BigDecimal("10.0"));
	}
	
	public Point2DBig[] intersectsWith(Equation equ, BigDecimal rangeFrom, BigDecimal rangeTo) {
		if (!this.isEqParsed() || !equ.isEqParsed()) return null; 
		if (rangeTo.subtract(rangeFrom, CalcConst.MC).compareTo(CalcConst.I_ACCURACY) < 0) System.out.println("WTF??");//throw new IllegalArgumentException("Invalid bounds for intersection search");
		final BigDecimal ZERO = BigDecimal.ZERO;
		final Point2DBig[] EMPTY = new Point2DBig[] { };
		
		try {
			if (this.getRootOp().isConstant() && equ.getRootOp().isConstant()) {
				if (this.isEqInX()) {
					if (equ.isEqInX()) return EMPTY;
					else return new Point2DBig[]{ new Point2DBig(equ.eval(ZERO), this.eval(ZERO)) };
				}
				else {
					if (equ.isEqInX()) return new Point2DBig[]{ new Point2DBig(this.eval(ZERO), equ.eval(ZERO)) };
					else return EMPTY;
				}
			}
			else if (equ.getRootOp().isConstant()) {
				if (this.isEqInX()) {
					if (!equ.isEqInX()) return new Point2DBig[]{ new Point2DBig(equ.eval(ZERO), this.eval(equ.eval(ZERO))) };
				}
				else {
					if (equ.isEqInX()) return new Point2DBig[]{ new Point2DBig(this.eval(equ.eval(ZERO)), equ.eval(ZERO)) };
				}
			}
			else if (this.getRootOp().isConstant()) {
				if (this.isEqInX()) {
					if (!equ.isEqInX()) return new Point2DBig[]{ new Point2DBig(this.eval(ZERO), equ.eval(this.eval(ZERO))) };
				}
				else {
					if (equ.isEqInX()) return new Point2DBig[]{ new Point2DBig(equ.eval(this.eval(ZERO)), this.eval(ZERO)) };
				}
			}
		}
		catch (OperatorDomainException eOD) { }
		
		ArrayList<Point2DBig> xList = new ArrayList<Point2DBig>();
		
		Equation xEq = null, yEq = null;
		boolean sameVar = !(this.isEqInX() ^ equ.isEqInX());
		if (!sameVar) {
			if (this.isEqInX()) { xEq = this; yEq = equ; }
			else { xEq = equ; yEq = this; }
		}
		
		BigDecimal lastDev = null;
		for (BigDecimal q = rangeFrom; q.compareTo(rangeTo) <= 0; q = q.add(CalcConst.I_ACCURACY, CalcConst.MC)) {
			BigDecimal dev;
			try {
				if (sameVar) dev = this.eval(q).subtract(equ.eval(q), CalcConst.MC);
				else dev = q.subtract(yEq.eval(xEq.eval(q)), CalcConst.MC);
			}
			catch (OperatorDomainException eOD) {
				lastDev = null;
				continue;
			}

			if (dev.compareTo(BigDecimal.ZERO) == 0) {
				try {
					if (sameVar) {
						if (this.isEqInX()) xList.add(new Point2DBig(q.round(CalcConst.MC), this.eval(q).round(CalcConst.MC)));
						else xList.add(new Point2DBig(this.eval(q).round(CalcConst.MC), q.round(CalcConst.MC)));
					}
					else {
						xList.add(new Point2DBig(q.round(CalcConst.MC), xEq.eval(q).round(CalcConst.MC)));
					}
				}
				catch (OperatorDomainException eOD) { }
				lastDev = null;
				continue;
			}
			
			if (lastDev == null) {
				lastDev = dev;
				continue;
			}
			
			if (CalcUtils.linesIntersect(q.subtract(CalcConst.I_ACCURACY, CalcConst.MC), lastDev, q, dev, q.subtract(CalcConst.I_ACCURACY, CalcConst.MC), BigDecimal.ZERO, q, BigDecimal.ZERO)) {
				//if (!this.isDiverging(q.subtract(CalcConst.I_ACCURACY, CalcConst.MC), q) && !equ.isDiverging(q.subtract(CalcConst.I_ACCURACY, CalcConst.MC), q)) 
				Point2DBig pt = intersectsWith(equ, q.subtract(CalcConst.I_ACCURACY, CalcConst.MC), q, CalcConst.I_ACCURACY.divide(BigDecimal.TEN, CalcConst.MC)); 
				if (pt != null) xList.add(pt);
			}
			lastDev = dev;
		}
		
		Point2DBig[] xPts = new Point2DBig[xList.size()];
		int q = 0;
		for (Iterator<Point2DBig> it = xList.iterator(); it.hasNext(); ) {
			xPts[q++] = it.next();
		}
		
		return xPts;
	}
	
	private Point2DBig intersectsWith(Equation equ, BigDecimal rangeFrom, BigDecimal rangeTo, BigDecimal accuracy) {
		Equation xEq = null, yEq = null;
		boolean sameVar = !(this.isEqInX() ^ equ.isEqInX());
		if (!sameVar) {
			if (this.isEqInX()) { xEq = this; yEq = equ; }
			else { xEq = equ; yEq = this; }
		}
		
		BigDecimal lastDev = null;
		
		for (BigDecimal q = rangeFrom; q.compareTo(rangeTo) <= 0; q = q.add(accuracy, CalcConst.MC)) {
			BigDecimal dev;
			try {
				if (sameVar) dev = this.eval(q).subtract(equ.eval(q), CalcConst.MC);
				else dev = q.subtract(yEq.eval(xEq.eval(q)), CalcConst.MC);
			}
			catch (OperatorDomainException eOD) {
				lastDev = null;
				continue;
			}

			if (dev.compareTo(new BigDecimal("0.0")) == 0) {
				try {
					if (sameVar) {
						if (this.isEqInX()) return new Point2DBig(q.round(CalcConst.MC), this.eval(q).round(CalcConst.MC));
						else return new Point2DBig(this.eval(q).round(CalcConst.MC), q.round(CalcConst.MC));
					}
					else {
						return new Point2DBig(q.round(CalcConst.MC), xEq.eval(q).round(CalcConst.MC));
					}
				}
				catch (OperatorDomainException eOD) {
					lastDev = null;
					continue;
				}
			}
			
			if (lastDev == null) {
				lastDev = dev;
				continue;
			}
			
			if (CalcUtils.linesIntersect(q.subtract(accuracy, CalcConst.MC), lastDev, q, dev, q.subtract(accuracy, CalcConst.MC), BigDecimal.ZERO, q, BigDecimal.ZERO)) {
				if (accuracy.compareTo(CalcConst.T_ACCURACY) >= 0) return intersectsWith(equ, q.subtract(accuracy, CalcConst.MC), q, accuracy.divide(BigDecimal.TEN, CalcConst.MC));
				else {
					//BigDecimal slp = (new Line2DBig(q.subtract(accuracy, CalcConst.MC), lastDev, q, dev)).getSlope();
					//if (BigDecimal.ONE.divide(slp, CalcConst.MC).compareTo(CalcConst.VERT_DIFF) <= 0) return null;
					BigDecimal retQ = q.subtract(accuracy.divide(new BigDecimal("2.0"), CalcConst.MC));
					try {
						if (sameVar) {
							if (this.isEqInX()) return new Point2DBig(retQ.round(CalcConst.MC), this.eval(retQ).round(CalcConst.MC));
							else return new Point2DBig(this.eval(retQ).round(CalcConst.MC), retQ.round(CalcConst.MC));
						}
						else {
							return new Point2DBig(retQ, xEq.eval(retQ).round(CalcConst.MC));
						}
					}
					catch (OperatorDomainException eOD) {
						return null;
					}
				}
			}
			lastDev = dev;
		}
		return null;
	}
	
	public String toString() {
		String out = "";
		out += "f(";
		out += indepVar;
		out += ")=";
		out += rootOperation.toString();
		out += ";;; is ";
		out += (isParsed) ? "parsed." : "not parsed.";
		return out;
	}
	
	public boolean isEqInX() {
		return indepVar.equals("x");
	}
	
	public Operation getRootOp() {
		return rootOperation;
	}
	
	public String getIndVar() {
		return indepVar;
	}
	
	public boolean isEqParsed() {
		return isParsed;
	}
}
