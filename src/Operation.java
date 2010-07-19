import java.math.BigDecimal;

/**
 * @author Vetruvet
 * 
 */
public class Operation {
	private boolean TWO_OP;
	private Object OPERAND1, OPERAND2;
	private Operand OPCODE;
	private boolean opCodeSet, op1Set, op2Set;
	
	/**
	 * @param opCode
	 * @param op1
	 * @param op2
	 */
	public Operation(Operand opCode, Object op1, Object op2) {
		if (!((op1 instanceof BigDecimal) || (op1 instanceof Operation))) throw new IllegalArgumentException("Invalid Operand");
		if (!((op2 instanceof BigDecimal) || (op2 instanceof Operation))) throw new IllegalArgumentException("Invalid Operand");
		TWO_OP = true;
		OPCODE = opCode;
		OPERAND1 = op1;
		OPERAND2 = op2;
		opCodeSet = op1Set = op2Set = true;
	}
	
	/**
	 * @param opCode
	 * @param op
	 */
	public Operation(Operand opCode, Object op) {
		if (!((op instanceof BigDecimal) || (op instanceof Operation))) throw new IllegalArgumentException("Invalid Operand");
		TWO_OP = false;
		OPCODE = opCode;
		OPERAND1 = op;
		OPERAND2 = null;
		opCodeSet = op1Set = op2Set = true;
	}
	
	/**
	 * @param opCode
	 */
	public Operation(Operand opCode) {
		OPCODE = opCode;
		switch (OPCODE) {
		case PLUS:
		case MINUS:
		case MULT:
		case DIV:
		case POW:
			TWO_OP = true; break;
		default:
			TWO_OP = false; break;
		}
		opCodeSet = true;
		op1Set = op2Set = false;
	}
	
	/**
	 * 
	 */
	public Operation() { }
	
	/**
	 * @return
	 */
	public boolean isConstant() {
		if (OPCODE == Operand.CONSTANT) return true;
		if (OPCODE == Operand.INDEP_VAR) return false;
		if (TWO_OP) {
			if (OPERAND1 instanceof BigDecimal) {
				if (OPERAND2 instanceof BigDecimal) {
					return true;
				}
				else if (((Operation) OPERAND2).isConstant()) {
					return true;
				}
			}
			else if (((Operation) OPERAND1).isConstant()) {
				if (OPERAND2 instanceof BigDecimal) {
					return true;
				}
				else if (((Operation) OPERAND2).isConstant()) {
					return true;
				}
			}
		}
		else {
			if (OPERAND1 instanceof BigDecimal) return true;
			else if (((Operation) OPERAND1).isConstant()) return true;
		}
		return false;
	}
	
	/**
	 * @param indVar
	 * @return
	 * @throws InvalidOperatorException
	 * @throws OperatorDomainException
	 */
	public BigDecimal eval(BigDecimal indVar) throws InvalidOperatorException, OperatorDomainException {
		if (OPCODE == Operand.INDEP_VAR) return indVar.round(CalcConst.MC);
		if (OPCODE == Operand.CONSTANT) return ((BigDecimal) OPERAND1).round(CalcConst.MC);
		
		if (TWO_OP) {
			BigDecimal op1, op2;
			op1 = op2 = null;
			if (OPERAND1 instanceof Operation) {
				op1 = ((Operation) OPERAND1).eval(indVar);
			}
			else if (OPERAND1 instanceof BigDecimal) {
				op1 = ((BigDecimal) OPERAND1);
			}
			if (OPERAND2 instanceof Operation) {
				op2 = ((Operation) OPERAND2).eval(indVar);
			}
			else if (OPERAND2 instanceof BigDecimal) {
				op2 = ((BigDecimal) OPERAND2);
			}
			
			try {
				switch (OPCODE) {
				case PLUS:
					return op1.add(op2, CalcConst.MC);
				case MINUS:
					return op1.subtract(op2, CalcConst.MC);
				case MULT:
					return op1.multiply(op2, CalcConst.MC);
				case DIV:
					return op1.divide(op2, CalcConst.MC);
				case POW:
					return new BigDecimal(Math.pow(op1.doubleValue(), op2.doubleValue()), CalcConst.MC);
				default:
					throw new InvalidOperatorException("Invalid Opertor for 2-variable operation");
				}
			}
			catch (ArithmeticException eA) {
				throw new OperatorDomainException("Input is outside operator's domain");
			}
		}
		else {
			BigDecimal op;
			op = null;
			if (OPERAND1 instanceof Operation) {
				op = ((Operation) OPERAND1).eval(indVar);
			}
			else if (OPERAND1 instanceof BigDecimal) {
				op = ((BigDecimal) OPERAND1);
			}
			
			try {
				switch(OPCODE) {
				case SQRT:
					return new BigDecimal(Math.sqrt(op.doubleValue()), CalcConst.MC);
				case CBRT:
					return new BigDecimal(Math.cbrt(op.doubleValue()), CalcConst.MC);
				case LOG:
					return new BigDecimal(Math.log10(op.doubleValue()), CalcConst.MC);
				case NLOG:
					return new BigDecimal(Math.log(op.doubleValue()), CalcConst.MC);
				case SIN:
					return new BigDecimal(Math.sin(op.doubleValue()), CalcConst.MC);
				case COS:
					return new BigDecimal(Math.cos(op.doubleValue()), CalcConst.MC);
				case TAN:
					return new BigDecimal(Math.tan(op.doubleValue()), CalcConst.MC);
				case CSC:
					return new BigDecimal(1.0 / Math.sin(op.doubleValue()), CalcConst.MC);
				case SEC:
					return new BigDecimal(1.0 / Math.cos(op.doubleValue()), CalcConst.MC);
				case COT:
					return new BigDecimal(1.0 / Math.tan(op.doubleValue()), CalcConst.MC);
				case ARCSIN:
					return new BigDecimal(Math.asin(op.doubleValue()), CalcConst.MC);
				case ARCCOS:
					return new BigDecimal(Math.acos(op.doubleValue()), CalcConst.MC);
				case ARCTAN:
					return new BigDecimal(Math.atan(op.doubleValue()), CalcConst.MC);
				case ARCCSC:
					return new BigDecimal(Math.asin(1.0 / op.doubleValue()), CalcConst.MC);
				case ARCSEC:
					return new BigDecimal(Math.acos(1.0 / op.doubleValue()), CalcConst.MC);
				case ARCCOT:
					return new BigDecimal(Math.atan(1.0 / op.doubleValue()), CalcConst.MC);
				case NEG:
					return op.negate(CalcConst.MC);
				case ABS:
					return op.abs(CalcConst.MC);
				default:
					throw new InvalidOperatorException("Invalid Operator for 1-variable operation");
				}
			}
			catch (NumberFormatException eNF) {
				throw new OperatorDomainException("Input is outside operator's domain");
			}
			catch (ArithmeticException eA) {
				throw new OperatorDomainException("Input is outside operator's domain");
			}
		}
	}
	
	/**
	 * @param opCode
	 */
	public void setOpCode(Operand opCode) {
		OPCODE = opCode;
		opCodeSet = true;
		switch (OPCODE) {
		case PLUS:
		case MINUS:
		case MULT:
		case DIV:
		case POW:
			TWO_OP = true; break;
		default:
			TWO_OP = false; break;
		}
	}
	
	/**
	 * @return
	 */
	public Operand getOpCode() {
		return OPCODE;
	}
	
	/**
	 * @param op
	 */
	public void setOperand1(Object op) {
		if (!opCodeSet) return;
		if (!op1Set) {
			OPERAND1 = op;
			if (TWO_OP) op1Set = true;
			else {
				op1Set = op2Set = true;
				OPERAND2 = null;
			}
		}
	}
	
	/**
	 * @param op
	 */
	public void setOperand2(Object op) {
		if (!opCodeSet) return;
		if (!op2Set) {
			OPERAND2 = op;
			op2Set = true;
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		switch (OPCODE) {
		case PLUS:
		case MINUS:
		case MULT:
		case DIV:
		case POW:
			return "(" + OPERAND1.toString() + ")" + CalcUtils.operandAsString(OPCODE) + "(" + OPERAND2.toString() + ")";
		case SQRT:
		case CBRT:
		case LOG:
		case NLOG:
		case SIN:
		case COS:
		case TAN:
		case CSC:
		case SEC:
		case COT:
		case ARCSIN:
		case ARCCOS:
		case ARCTAN:
		case ARCCSC:
		case ARCSEC:
		case ARCCOT:
		case NEG:
			return CalcUtils.operandAsString(OPCODE) + "(" + OPERAND1.toString() + ")";
		case INDEP_VAR:
			return CalcUtils.operandAsString(OPCODE);
		case CONSTANT:
			return "(" + ((BigDecimal) OPERAND1).toString() + ")";
		default:
			return "";
		}
	}
}
