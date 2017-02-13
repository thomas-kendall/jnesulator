package jnesulator.core.nes.mapper;

import jnesulator.core.nes.NES;
import jnesulator.core.nes.ROMLoader;

public class MapperLoader {
	public static BaseMapper getCorrectMapper(NES nes, ROMLoader l) throws BadMapperException {
		int type = l.mappertype;
		boolean haschr = (l.chrsize == 0);
		switch (type) {
		case -1:
			return new NSFMapper(nes);
		case 0:
			return new NromMapper(nes);
		case 1:
			return new MMC1Mapper(nes);
		case 2:
			return new UnromMapper(nes);
		case 3:
			return new CnromMapper(nes);
		case 4:
			return new MMC3Mapper(nes);
		case 5:
			return new MMC5Mapper(nes);
		case 7:
			return new AnromMapper(nes);
		case 9:
			return new MMC2Mapper(nes);
		case 10:
			return new MMC4Mapper(nes);
		case 11:
			return new ColorDreamsMapper(nes);
		case 15:
		case 169:
			return new Mapper15(nes);
		case 19:
			return new NamcoMapper(nes);
		case 21:
		case 23:
		case 25:
			// VRC4 has three different mapper numbers for six different address
			// line layouts
			// some of which really should be VRC2
			// but they're all handled in the same file
			// there's a proposal for submapper #s in iNES 2.0
			return new VRC4Mapper(nes, type);
		case 22:
			return new VRC2Mapper(nes);
		case 24:
		case 26:
			return new VRC6Mapper(nes, type);
		case 31:
			return new Mapper31(nes);
		case 33:
			return new Mapper33(nes);
		case 34:
			if (haschr) {
				return new BnromMapper(nes);
			} else {
				return new NINA_001_Mapper(nes);
			}
		case 36:
			return new Mapper36(nes);
		case 38:
			return new CrimeBustersMapper(nes);
		case 41:
			return new CaltronMapper(nes);
		case 47:
			return new Mapper47(nes);
		case 48:
			return new Mapper48(nes);
		case 58:
			return new Mapper58(nes);
		case 60:
			return new Mapper60(nes);
		case 61:
			return new Mapper61(nes);
		case 62:
			return new Mapper62(nes);
		case 64:
			return new TengenRamboMapper(nes);
		case 65:
			return new IremH3001Mapper(nes);
		case 66:
			return new GnromMapper(nes);
		case 67:
			return new Sunsoft03Mapper(nes);
		case 68:
			return new AfterburnerMapper(nes);
		case 69:
			return new FME7Mapper(nes);
		case 70:
			return new Mapper70(nes);
		case 71:
			return new CodemastersMapper(nes);
		case 72:
			return new Mapper72(nes);
		case 73:
			return new VRC3Mapper(nes);
		case 75:
			return new VRC1Mapper(nes);
		case 76:
			return new Mapper76(nes);
		case 78:
			return new Mapper78(nes);
		case 79:
		case 113:
			return new NINA_003_006_Mapper(nes, type);
		case 85:
			return new VRC7Mapper(nes);
		case 86:
			return new Mapper86(nes);
		case 87:
			return new Mapper87(nes);
		case 88:
		case 154:
			return new Namcot34x3Mapper(nes, type);
		case 89:
		case 93:
			return new Sunsoft02Mapper(nes, type);
		case 92:
			return new Mapper92(nes);
		case 94:
			return new Mapper94(nes);
		case 97:
			return new Mapper97(nes);
		case 107:
			return new Mapper107(nes);
		case 112:
			return new Mapper112(nes);
		case 119:
			return new Mapper119(nes);
		case 140:
			return new Mapper140(nes);
		case 152:
			return new Mapper152(nes);
		case 180:
			return new CrazyClimberMapper(nes);
		case 182:
			return new Mapper182(nes);
		case 184:
			return new Sunsoft01Mapper(nes);
		case 185:
			return new Mapper185(nes);
		case 200:
			return new Mapper200(nes);
		case 201:
			return new Mapper201(nes);
		case 203:
			return new Mapper203(nes);
		case 206:
			return new MIMICMapper(nes);
		case 212:
			return new Mapper212(nes);
		case 213:
			return new Mapper213(nes);
		case 214:
			return new Mapper214(nes);
		case 225:
			return new Mapper225(nes);
		case 226:
			return new Mapper226(nes);
		case 228:
			return new Action52Mapper(nes);
		case 229:
			return new Mapper229(nes);
		case 231:
			return new Mapper231(nes);
		case 240:
			return new Mapper240(nes);
		case 241:
			return new Mapper241(nes);
		case 242:
			return new Mapper242(nes);
		case 244:
			return new Mapper244(nes);
		case 246:
			return new Mapper246(nes);
		case 255:
			return new Mapper255(nes);
		default:
			System.err.println("unsupported mapper # " + type);
			throw new BadMapperException("Unsupported mapper: " + type);
		}
	}

}
